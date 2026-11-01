package com.sky.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工信息返回对象。
 *
 * <p>不包含 password，避免密码哈希返回给前端。</p>
 */
@Data
@Schema(description = "员工信息")
public class EmployeeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 员工 id */
    @Schema(description = "员工id")
    private Long id;

    /** 姓名 */
    @Schema(description = "姓名")
    private String name;

    /** 登录用户名 */
    @Schema(description = "用户名")
    private String username;

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
}
