package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.SetmealDish;

/**
 * 套餐菜品关系 Mapper。
 *
 * <p>新增/修改套餐时由 Service 统一维护套餐和菜品的关系。</p>
 */
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {
}
