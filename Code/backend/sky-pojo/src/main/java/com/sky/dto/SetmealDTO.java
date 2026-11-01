package com.sky.dto;

import com.sky.entity.SetmealDish;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 套餐新增/修改入参。
 *
 * <p>setmealDishes 为套餐包含的菜品列表；新增和修改时可以一起提交。</p>
 */
@Data
@Schema(description = "套餐新增/修改")
public class SetmealDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，修改时必填 */
    @Schema(description = "主键，修改时必填")
    private Long id;

    /** 套餐分类 id，必须是 type=2 的分类 */
    @NotNull(message = "套餐分类不能为空")
    @Schema(description = "套餐分类id")
    private Long categoryId;

    /** 套餐名称 */
    @NotBlank(message = "套餐名称不能为空")
    @Schema(description = "套餐名称")
    private String name;

    /** 套餐价格 */
    @NotNull(message = "套餐价格不能为空")
    @DecimalMin(value = "0.01", message = "套餐价格必须大于0")
    @Schema(description = "套餐价格")
    private BigDecimal price;

    /** 状态：0 停售，1 起售 */
    @Schema(description = "状态：0停售 1起售")
    private Integer status;

    /** 描述信息 */
    @Schema(description = "描述信息")
    private String description;

    /** 图片路径 */
    @Schema(description = "图片")
    private String image;

    /** 套餐包含的菜品 */
    @Schema(description = "套餐菜品列表")
    private List<SetmealDish> setmealDishes;
}
