package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 向订单表中插入数据
     * @param order
     */
    void insert(Orders order);

    /**
     * 动态获取订单信息
     * @param orders
     * @return
     */
    Page<OrderVO> list(Orders orders);

    /**
     * 根据订单id查询订单基本信息
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 修改订单信息
     * @param order
     */
    void update(Orders order);

    /**
     * 根据订单状态status和下单时间orderTime查询订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /**
     * 统计当前日期的营业额
     * @param begin
     * @return
     */
    Double getByDate(LocalDateTime begin, LocalDateTime end);

    /**
     * 统计当前日期和条件下的订单数
     * @param begin
     * @param end
     * @param status
     * @return
     */
    Integer getCountByDate(LocalDateTime begin, LocalDateTime end, Integer status);

    /**
    *根据动态条件统计订单数量
    * @param map
    */
    Integer countByMap(Map map);

    /**
     * 根据动态条件统计营业额
     * @param map
     */
    Double sumByMap(Map map);
}
