package com.sky.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/** 用户登录入参：code 先作为 mock openid 使用。 */
@Data
public class UserLoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "登录code不能为空")
    private String code;
}
