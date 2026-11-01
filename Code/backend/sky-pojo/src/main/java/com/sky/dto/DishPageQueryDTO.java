package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 菜品分页查询入参。
 *
 * <p>name 模糊匹配，categoryId、status 精确匹配。</p>
 */
@Data
@Schema(description = "菜品分页查询")
public class DishPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码，从 1 开始 */
    @Schema(description = "页码", defaultValue = "1")
    private Integer page = 1;

    /** 每页记录数 */
    @Schema(description = "每页记录数", defaultValue = "10")
    private Integer pageSize = 10;

    /** 菜品名称，模糊查询 */
    @Schema(description = "菜品名称")
    private String name;

    /** 菜品分类 id */
    @Schema(description = "菜品分类id")
    private Long categoryId;

    /** 状态：0 停售，1 起售 */
    @Schema(description = "状态：0停售 1起售")
    private Integer status;
}
