package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Setmeal;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 套餐 Mapper。
 *
 * <p>继承 BaseMapper 获得套餐单表 CRUD；额外检查订单是否引用该套餐。</p>
 */
public interface SetmealMapper extends BaseMapper<Setmeal> {

    /**
     * 统计某个套餐被多少条订单明细引用。
     *
     * @param setmealId 套餐 id
     * @return 引用数量
     */
    @Select("SELECT COUNT(*) FROM order_detail WHERE setmeal_id = #{setmealId}")
    long countOrderDetail(@Param("setmealId") Long setmealId);
}
