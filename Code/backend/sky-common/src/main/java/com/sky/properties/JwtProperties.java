package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性。
 *
 * <p>对应 application.yml 里的 sky.jwt 配置，管理端和用户端各用一套密钥与请求头。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "sky.jwt")
public class JwtProperties {

    /** 管理端 token 签名密钥 */
    private String adminSecretKey;

    /** 管理端 token 有效期，单位毫秒 */
    private Long adminTtl;

    /** 管理端 token 请求头名称，例如 token */
    private String adminTokenName;

    /** 用户端 token 签名密钥 */
    private String userSecretKey;

    /** 用户端 token 有效期，单位毫秒 */
    private Long userTtl;

    /** 用户端 token 请求头名称，例如 authentication */
    private String userTokenName;
}
