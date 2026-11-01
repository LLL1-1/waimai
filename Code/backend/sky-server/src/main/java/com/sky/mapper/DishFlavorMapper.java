package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.DishFlavor;

/**
 * 菜品口味 Mapper。
 *
 * <p>一个菜品对应多条口味记录，新增/修改菜品时由 Service 统一维护。</p>
 */
public interface DishFlavorMapper extends BaseMapper<DishFlavor> {
}
