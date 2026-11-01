package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Dish;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 菜品 Mapper。
 *
 * <p>继承 BaseMapper 获得菜品单表 CRUD；额外提供一个检查套餐引用的查询。</p>
 */
public interface DishMapper extends BaseMapper<Dish> {

    /**
     * 统计某个菜品被多少个套餐引用。
     *
     * @param dishId 菜品 id
     * @return 引用数量
     */
    @Select("SELECT COUNT(*) FROM setmeal_dish WHERE dish_id = #{dishId}")
    long countSetmealDish(@Param("dishId") Long dishId);
}
