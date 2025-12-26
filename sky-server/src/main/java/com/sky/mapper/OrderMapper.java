package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
    OrderVO getById(Long id);
}
