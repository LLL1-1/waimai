package com.sky.vo;

import com.sky.entity.SetmealDish;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 套餐返回对象。
 *
 * <p>比实体多 categoryName 和 setmealDishes，方便前端列表/编辑页直接使用。</p>
 */
@Data
@Schema(description = "套餐信息")
public class SetmealVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 套餐 id */
    @Schema(description = "套餐id")
    private Long id;

    /** 套餐分类 id */
    @Schema(description = "套餐分类id")
    private Long categoryId;

    /** 分类名称 */
    @Schema(description = "分类名称")
    private String categoryName;

    /** 套餐名称 */
    @Schema(description = "套餐名称")
    private String name;

    /** 套餐价格 */
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

    /** 更新时间 */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /** 套餐包含的菜品 */
    @Schema(description = "套餐菜品列表")
    private List<SetmealDish> setmealDishes;
}
