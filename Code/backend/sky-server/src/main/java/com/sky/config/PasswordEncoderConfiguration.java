package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置。
 *
 * <p>只引入 spring-security-crypto，不启用完整的 Spring Security 过滤器链。
 * 员工密码使用 BCrypt 存储和校验。</p>
 */
@Configuration
public class PasswordEncoderConfiguration {

    /**
     * 注册 BCrypt 密码编码器。
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
