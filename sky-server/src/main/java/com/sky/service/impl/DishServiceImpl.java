package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和对应的口味数据
     * @param dishDTO
     */
    // 涉及多个表，需要保证数据的一致性
    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        // 插入1条数据到菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        // 获取insert语句生成的主键值
        Long dishId = dish.getId();

        // 插入n条数据到口味表
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors == null || flavors.size() == 0) {
            return;
        }

        // TODO 这两步可以优化，直接往mapper层传参
        flavors.forEach(flavor -> flavor.setDishId(dishId));
        dishFlavorMapper.insertBatch(flavors);
    }
}
