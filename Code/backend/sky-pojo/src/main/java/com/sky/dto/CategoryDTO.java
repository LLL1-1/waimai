package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类新增/修改入参。
 *
 * <p>新增时 id 为空；修改时 id 必填。name 和 type 由 @Valid 做基础校验。</p>
 */
@Data
@Schema(description = "分类新增/修改")
public class CategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，修改时必填 */
    @Schema(description = "主键，修改时必填")
    private Long id;

    /** 分类名称，不能为空 */
    @NotBlank(message = "分类名称不能为空")
    @Schema(description = "分类名称")
    private String name;

    /** 分类类型：1 菜品分类，2 套餐分类 */
    @NotNull(message = "分类类型不能为空")
    @Schema(description = "类型：1菜品分类 2套餐分类")
    private Integer type;

    /** 排序值，新增不传时默认 0 */
    @Schema(description = "顺序")
    private Integer sort;

    /** 状态，新增不传时默认启用 */
    @Schema(description = "状态：0禁用 1启用")
    private Integer status;
}
