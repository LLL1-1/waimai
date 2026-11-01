package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工新增/修改入参。
 *
 * <p>新增时 password 必填；修改时 password 为空表示不修改密码。
 * id 在修改时必填。</p>
 */
@Data
@Schema(description = "员工新增/修改")
public class EmployeeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，修改时必填 */
    @Schema(description = "主键，修改时必填")
    private Long id;

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名")
    private String name;

    /** 登录用户名，数据库有唯一索引 */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    /** 明文密码；新增必填，修改不填表示不改 */
    @Schema(description = "密码；新增必填，修改不填表示不改")
    private String password;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    private String phone;

    /** 性别 */
    @NotBlank(message = "性别不能为空")
    @Schema(description = "性别")
    private String sex;

    /** 身份证号 */
    @NotBlank(message = "身份证号不能为空")
    @Schema(description = "身份证号")
    private String idNumber;

    /** 状态：0 禁用，1 启用；新增不传默认启用 */
    @Schema(description = "状态：0禁用 1启用")
    private Integer status;
}
