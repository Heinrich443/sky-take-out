package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，定时处理订单状态
 */
@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单的方法
     * 每分钟触发一次
     */
    @Scheduled(cron = "0 * * * * ?")
    // @Scheduled(cron = "0/5 * * * * ?")
    public void processTimeOutOrder() {
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        // select * from orders where status = ? and order_time < (now - 15min)
        // 15分钟未付款则取消订单
        List<Orders> orders = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));
        if (orders == null || orders.isEmpty()) {
            return;
        }

        for (Orders order : orders) {
            order.setStatus(Orders.CANCELLED);
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason(MessageConstant.CANCEL_BY_TASK);
            orderMapper.update(order);
        }
    }

    /**
     * 处理一直处于派送中的订单
     * 每小时触发
     */
    @Scheduled(cron = "0 0 * * * ?")
    // @Scheduled(cron = "1/5 * * * * ?")
    public void processDeliveryOrder() {
        log.info("定时处理处于派送中的订单：{}", LocalDateTime.now());
        // 派送超过3小时自动确认订单状态
        List<Orders> orders = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusHours(3));
        if (orders == null || orders.isEmpty()) {
            return;
        }

        for (Orders order : orders) {
            order.setStatus(Orders.COMPLETED);
            orderMapper.update(order);
        }
    }
}
