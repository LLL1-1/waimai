package com.sky.dto;

import com.sky.entity.DishFlavor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 菜品新增/修改入参。
 *
 * <p>flavors 为菜品口味列表；新增和修改时可以一起提交。</p>
 */
@Data
@Schema(description = "菜品新增/修改")
public class DishDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，修改时必填 */
    @Schema(description = "主键，修改时必填")
    private Long id;

    /** 菜品名称 */
    @NotBlank(message = "菜品名称不能为空")
    @Schema(description = "菜品名称")
    private String name;

    /** 菜品分类 id，必须是 type=1 的分类 */
    @NotNull(message = "菜品分类不能为空")
    @Schema(description = "菜品分类id")
    private Long categoryId;

    /** 菜品价格 */
    @NotNull(message = "菜品价格不能为空")
    @DecimalMin(value = "0.01", message = "菜品价格必须大于0")
    @Schema(description = "菜品价格")
    private BigDecimal price;

    /** 图片路径 */
    @Schema(description = "图片")
    private String image;

    /** 描述信息 */
    @Schema(description = "描述信息")
    private String description;

    /** 状态：0 停售，1 起售 */
    @Schema(description = "状态：0停售 1起售")
    private Integer status;

    /** 菜品口味列表 */
    @Schema(description = "菜品口味列表")
    private List<DishFlavor> flavors;
}
