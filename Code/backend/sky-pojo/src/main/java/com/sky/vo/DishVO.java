package com.sky.vo;

import com.sky.entity.DishFlavor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜品返回对象。
 *
 * <p>比实体多 categoryName 和 flavors，方便前端列表/编辑页直接使用。</p>
 */
@Data
@Schema(description = "菜品信息")
public class DishVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 菜品 id */
    @Schema(description = "菜品id")
    private Long id;

    /** 菜品名称 */
    @Schema(description = "菜品名称")
    private String name;

    /** 菜品分类 id */
    @Schema(description = "菜品分类id")
    private Long categoryId;

    /** 分类名称 */
    @Schema(description = "分类名称")
    private String categoryName;

    /** 菜品价格 */
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

    /** 更新时间 */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /** 菜品口味列表 */
    @Schema(description = "菜品口味列表")
    private List<DishFlavor> flavors;
}
