package com.sky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体，对应 employee 表。
 *
 * <p>管理端登录、员工管理都使用这张表；password 存的是 BCrypt 哈希。</p>
 */
@Data
@TableName("employee")
@Schema(description = "员工")
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    /** 姓名 */
    @Schema(description = "姓名")
    private String name;

    /** 登录用户名，数据库有唯一索引 */
    @Schema(description = "用户名")
    private String username;

    /** BCrypt 密码哈希，不返回给前端 */
    @Schema(description = "密码哈希")
    private String password;

    /** 手机号 */
    @Schema(description = "手机号")
    private String phone;

    /** 性别 */
    @Schema(description = "性别")
    private String sex;

    /** 身份证号 */
    @Schema(description = "身份证号")
    private String idNumber;

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
