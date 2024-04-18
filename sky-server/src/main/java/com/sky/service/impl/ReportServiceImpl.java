package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Dish;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.OrderService;
import com.sky.service.ReportService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    /**
     * 统计指定时间区间内的营业额
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)){
            //日期计算，计算指定日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询date日期对应的营业额数据，营业额是指:状态为“已完成”的订单金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //select sum(amount) from order where order_time > ? and order_time < ? and status = 5
            Map<String,Object> map = new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnoverDouble = orderMapper.sumByMap(map);
            turnoverDouble = turnoverDouble == null ? 0.0 : turnoverDouble;
            turnoverList.add(turnoverDouble);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    /**
     * 统计指定时间区间内的用户量
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)){
            //日期计算，计算指定日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //存放每天的新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        //存放每天总的用户数量
        List<Integer> totalUsersList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map<String, Object> map = new HashMap<>();
            map.put("end",endTime);
            //总用户量列表 - select count(id) from user where create_time < ?;
            Integer totalUsers = userMapper.countByMap(map);
            totalUsers = totalUsers == null ? 0 : totalUsers;
            //新增用户数 - select count(id) from user where create_time > ? and create_time < ?;
            map.put("begin",beginTime);
            Integer newUsers = userMapper.countByMap(map);
            totalUsersList.add(totalUsers);
            newUserList.add(newUsers);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUsersList,","))
                .build();
    }

    /**
     * 统计指定时间区间内的订单数量
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        Integer allValidOrderCount = 0;
        Integer totalOrderCount = 0;
        //存放每天的订单总数
        ArrayList<Integer> orderCountList = new ArrayList<>();
        //存放每天的有效订单数
        ArrayList<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //每日订单数列表 - select count(id) from orders where order_time > ? and order_time < ?;
            Integer dayOrderCount = getOrderCount(beginTime,endTime,null);
            //订单总数
            totalOrderCount += dayOrderCount;
            //每日有效订单数列表 - select count(id) from orders where order_time > ? and order_time < ? and status = 5;
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            allValidOrderCount += validOrderCount;
            orderCountList.add(dayOrderCount);
            validOrderCountList.add(validOrderCount);
        }

        /*
        计算时间区间内的订单总数和有效订单数量 另一种写法
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        Integer allValidOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
         */

        Double orderCompletionRate = 0.0;
        if(totalOrderCount.doubleValue() != 0)
            orderCompletionRate = (allValidOrderCount.doubleValue() / totalOrderCount.doubleValue()) * 100;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(allValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 根据条件统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin,LocalDateTime end,Integer status){
        Map<String, Object> map = new HashMap<>();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);

        return orderMapper.countByMap(map);
    }

    /**
     * 查询指定时间区间内销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin,LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end,LocalTime.MAX);
        Map<String, Object> map = new HashMap<>();
        map.put("begin",beginTime);
        map.put("end",endTime);
        //select sum(od.number) number,od.name from order_detail od,orders o
        // where od.order_id = o.id and o.order_time > ? and o.order_time < ? and o.status = 5
        // group by od.name
        // order by number desc
        // limit 0,10;
        List<GoodsSalesDTO> orderDetailList = orderDetailMapper.saleTop10ByMap(map);
        //商品名称列表
        List<String> nameList = orderDetailList.stream().filter(name -> name.getName() != null)
                .map(GoodsSalesDTO::getName).collect(Collectors.toList());
        //销售列表
        List<Integer> numberList = orderDetailList.stream().filter(number -> number.getNumber() != 0)
                .map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }
}
