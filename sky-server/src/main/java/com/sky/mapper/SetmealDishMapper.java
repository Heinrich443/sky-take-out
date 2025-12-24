package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询套餐id
     * @param ids
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> ids);

    /**
     * 批量添加套餐-菜品关系信息
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据 setmealId 批量删除套餐-菜品关系信息
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据setmealId批量查询菜品
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getBatchBySetmealId(Long id);
}
