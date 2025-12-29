package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单明细
     * @param details
     */
    void insertBatch(List<OrderDetail> details);

    /**
     * 根据订单id查询订单详情
     * @param id
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long id);

    /**
     * 根据指定时间区间查询销量排名Top10的菜品和销量
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getNameAndCount(LocalDate begin, LocalDate end, Integer status);
}
