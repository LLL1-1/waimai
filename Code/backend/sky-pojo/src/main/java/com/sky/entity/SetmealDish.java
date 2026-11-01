package com.sky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 套餐菜品关系实体，对应 setmeal_dish 表。
 *
 * <p>name、price 是下单时的冗余字段，避免菜品改名/改价后影响历史套餐。</p>
 */
@Data
@TableName("setmeal_dish")
@Schema(description = "套餐菜品关系")
public class SetmealDish implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    /** 套餐 id */
    @Schema(description = "套餐id")
    private Long setmealId;

    /** 菜品 id */
    @Schema(description = "菜品id")
    private Long dishId;

    /** 菜品名称（冗余） */
    @Schema(description = "菜品名称")
    private String name;

    /** 菜品单价（冗余） */
    @Schema(description = "菜品单价")
    private BigDecimal price;

    /** 菜品份数 */
    @Schema(description = "菜品份数")
    private Integer copies;
}
