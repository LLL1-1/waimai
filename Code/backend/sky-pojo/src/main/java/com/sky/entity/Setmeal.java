package com.sky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 套餐实体，对应 setmeal 表。
 *
 * <p>category_id 关联 category 表，且分类的 type 必须是 2（套餐分类）。</p>
 */
@Data
@TableName("setmeal")
@Schema(description = "套餐")
public class Setmeal implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    /** 套餐分类 id */
    @Schema(description = "套餐分类id")
    private Long categoryId;

    /** 套餐名称，数据库有唯一索引 */
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
