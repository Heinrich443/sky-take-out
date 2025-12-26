package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 处理各种业务异常（地址簿为空，购物车为空）
        AddressBook address = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (address == null) {
            // 地址簿为空，抛出异常
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.isEmpty()) {
            // 购物车为空，抛出异常
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 向订单表中插入1条数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        order.setUserId(userId);
        order.setOrderTime(LocalDateTime.now());
        order.setConsignee(address.getConsignee());
        order.setPayStatus(Orders.UN_PAID);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPhone(address.getPhone());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        orderMapper.insert(order);

        // 向订单明细表中插入n条数据
        List<OrderDetail> details = new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(order.getId());
            details.add(orderDetail);
        }
        orderDetailMapper.insertBatch(details);

        // 清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        // 封装VO对象并返回
        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        orderSubmitVO.setId(order.getId());
        orderSubmitVO.setOrderTime(order.getOrderTime());
        orderSubmitVO.setOrderAmount(order.getAmount());
        orderSubmitVO.setOrderNumber(order.getNumber());

        return orderSubmitVO;
    }

    /**
     * 查询历史订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult history(String page, String pageSize, String status) {
        // 开启分页
        PageHelper.startPage(Integer.parseInt(page), Integer.parseInt(pageSize));

        // 获取订单基本信息 List<OrdersDto>
        Orders orders = new Orders();
        orders.setUserId(BaseContext.getCurrentId());
        if (status != null && !status.isEmpty()) {
            orders.setStatus(Integer.parseInt(status));
        }
        Page<OrderVO> orderVOPage = orderMapper.list(orders);
        List<OrderVO> list = orderVOPage.getResult();

        for (OrderVO orderVO : list) {
            // 获取订单详细菜品信息 List<OrdersDetail>
            List<OrderDetail> details = orderDetailMapper.getByOrderId(orderVO.getId());
            orderVO.setOrderDetailList(details);
        }

        // 返回
        PageResult pageResult = new PageResult();
        pageResult.setTotal(orderVOPage.getTotal());
        pageResult.setRecords(list);
        return pageResult;
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO detail(Long id) {
        OrderVO orderVO = orderMapper.getById(id);
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(details);
        return orderVO;
    }
}
