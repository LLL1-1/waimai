package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工登录入参。
 */
@Data
@Schema(description = "员工登录参数")
public class EmployeeLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    /** 密码（明文，服务端用 BCrypt 校验） */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;
}
