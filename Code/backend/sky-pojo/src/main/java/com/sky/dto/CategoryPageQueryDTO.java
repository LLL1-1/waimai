package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类分页查询入参。
 *
 * <p>page/pageSize 不传时使用默认值；name 支持模糊查询，type 精确匹配。</p>
 */
@Data
@Schema(description = "分类分页查询")
public class CategoryPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码，从 1 开始 */
    @Schema(description = "页码", defaultValue = "1")
    private Integer page = 1;

    /** 每页记录数 */
    @Schema(description = "每页记录数", defaultValue = "10")
    private Integer pageSize = 10;

    /** 分类名称，模糊查询 */
    @Schema(description = "分类名称")
    private String name;

    /** 分类类型，精确匹配 */
    @Schema(description = "类型：1菜品分类 2套餐分类")
    private Integer type;
}
