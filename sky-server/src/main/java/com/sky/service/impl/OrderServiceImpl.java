package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.service.OrderService;
import com.sky.service.SetmealService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //业务异常处理 - 地址是否为空&当前用户购物车是否有数据
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null) throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList == null || shoppingCartList.size() == 0) throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);

        //向订单表插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orderMapper.insert(orders);
        //向订单详细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //清空购物车
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
        //封装OrderSubmitVO数据返回
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单 【注释：没有注册微信支付相关信息！！】
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        JSONObject jsonObject = new JSONObject();
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过websocket向客户端推送消息 type orderId content
        Map map = new HashMap();
        map.put("type",1);//1表示来单提醒 2表示用户催单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号：" + outTradeNo);

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    /**
     * 催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);

        if(orders == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);

        Map map = new HashMap();
        map.put("type",2);//1来电提醒 2催单
        map.put("orderId",id);
        map.put("content","订单号：" + orders.getNumber());

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repetition(Long id) {
        //1. 查询当前用户ID
        Long userId = BaseContext.getCurrentId();
        //2. 根据id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        //3. 将订单详情对象转换为购物车对象 - steamAPI
       List<ShoppingCart> shoppingCartList =  orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();
           // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x,shoppingCart,"id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

       //4.将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        //设置分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        // 分页条件查询
        Page<Orders> orderPage = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList = new ArrayList<>();
        // 查询出订单明细，并封装入OrderVO进行响应
        if(orderPage != null && orderPage.getTotal() > 0){
            for (Orders order : orderPage) {
                //根据订单id查询订单详细表
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(order.getId());
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order,orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(orderPage.getTotal(), orderVOList);
    }
}
