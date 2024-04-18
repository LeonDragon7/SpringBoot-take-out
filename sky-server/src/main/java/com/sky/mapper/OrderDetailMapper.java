package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {
    /**
     * 向订单详细表批量插入数据
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     * 根据订单id获取订单详细表
     * @param id
     * @return
     */
    @Select("select * from order_detail where order_id = #{id}")
    List<OrderDetail> getByOrderId(Long id);

    /**
     * 根据时间区间查询订单详细表的商品名称
     * @param map
     * @return
     */
    String nameByMap(Map<String,Object> map);

    /**
     * 根据时间区间查询销售排名top10
     * @param map
     * @return
     */
    List<GoodsSalesDTO> saleTop10ByMap(Map<String, Object> map);
}
