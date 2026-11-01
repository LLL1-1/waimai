package com.sky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 菜品口味实体，对应 dish_flavor 表。
 *
 * <p>一个菜品可以有多个口味，例如“辣度：不辣/微辣/中辣”。</p>
 */
@Data
@TableName("dish_flavor")
@Schema(description = "菜品口味")
public class DishFlavor implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    /** 所属菜品 id */
    @Schema(description = "菜品id")
    private Long dishId;

    /** 口味名称，例如“辣度” */
    @Schema(description = "口味名称")
    private String name;

    /** 口味值，通常是 JSON 数组字符串 */
    @Schema(description = "口味值")
    private String value;
}
