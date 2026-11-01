package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** 用户登录返回。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String openid;
    private String token;
}
