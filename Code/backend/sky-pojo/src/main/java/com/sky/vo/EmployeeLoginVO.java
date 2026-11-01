package com.sky.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 员工登录返回结果。
 *
 * <p>只返回前端需要的信息，不包含密码哈希。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "员工登录结果")
public class EmployeeLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 员工 id */
    @Schema(description = "员工id")
    private Long id;

    /** 登录用户名 */
    @Schema(description = "用户名")
    private String userName;

    /** 员工姓名 */
    @Schema(description = "姓名")
    private String name;

    /** JWT token */
    @Schema(description = "jwt token")
    private String token;
}
