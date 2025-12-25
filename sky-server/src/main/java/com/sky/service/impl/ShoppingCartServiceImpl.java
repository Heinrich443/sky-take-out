package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // 判断当前加入购物车中的商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list != null && !list.isEmpty()) {
            // 如果已经存在，只需将数量加 1
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.update(cart);
            return;
        }

        // 如果不存在，需要插入一条购物车数据
        Long dishId = shoppingCartDTO.getDishId();
        // 判断加入购物车的是菜品还是套餐
        if (dishId == null) {
            // 是套餐
            Long setmealId = shoppingCartDTO.getSetmealId();
            Setmeal setmeal = setmealMapper.getById(setmealId);
            shoppingCart.setAmount(setmeal.getPrice());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setName(setmeal.getName());
        } else {
            // 是菜品
            Dish dish = dishMapper.getById(dishId);
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        }

        //
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
    }
}
