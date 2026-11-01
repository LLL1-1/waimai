package com.sky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜品分类 / 套餐分类实体，对应 category 表。
 *
 * <p>type=1 表示菜品分类，type=2 表示套餐分类；status=0 禁用，status=1 启用。</p>
 */
@Data
@TableName("category")
@Schema(description = "菜品分类")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    /** 分类类型：1 菜品分类，2 套餐分类 */
    @Schema(description = "类型：1菜品分类 2套餐分类")
    private Integer type;

    /** 分类名称，数据库有唯一索引 */
    @Schema(description = "分类名称")
    private String name;

    /** 排序值，越小越靠前 */
    @Schema(description = "顺序")
    private Integer sort;

    /** 状态：0 禁用，1 启用 */
    @Schema(description = "状态：0禁用 1启用")
    private Integer status;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /** 创建人 id */
    @Schema(description = "创建人")
    private Long createUser;

    /** 修改人 id */
    @Schema(description = "修改人")
    private Long updateUser;
}
