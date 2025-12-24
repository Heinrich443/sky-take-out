package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Transactional
    @Override
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // setmeal.setStatus(StatusConstant.DISABLE);
        setmealMapper.insert(setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(dish -> dish.setSetmealId(setmeal.getId()));
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.page(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        // 判断是否有套餐处于启售状态，有则不能删除
        List<Setmeal> setmeals = setmealMapper.getBatch(ids);
        for (Setmeal setmeal : setmeals) {
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);

            }
        }

        // 批量删除套餐
        setmealMapper.deleteBatch(ids);

        // 批量删除套餐-菜品关系
        setmealDishMapper.deleteBatch(ids);
    }

    /**
     * 根据id查询套餐信息
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        // 查询套餐基本信息
        Setmeal setmeal = setmealMapper.getById(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);

        // 查询与套餐关联的菜品信息
        List<SetmealDish> dishes = setmealDishMapper.getBatchBySetmealId(id);

        // 拼接查询到的信息
        setmealVO.setSetmealDishes(dishes);
        return setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealVO
     */
    @Transactional
    @Override
    public void update(SetmealDTO setmealDTO) {
        // 属性复制
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 修改套餐基本属性
        setmealMapper.update(setmeal);

        // 删除套餐-菜品关系
        List<Long> list = new ArrayList<>();
        list.add(setmealDTO.getId());
        setmealDishMapper.deleteBatch(list);

        // 添加套餐-菜品关系
        Long setmealId = setmealDTO.getId();
        List<SetmealDish> dishes = setmealDTO.getSetmealDishes();
        dishes.forEach(dish -> dish.setSetmealId(setmealId));
        setmealDishMapper.insertBatch(dishes);
    }
}
