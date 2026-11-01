package com.sky.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger 配置。
 *
 * <p>在 Swagger UI 上增加 Authorize 按钮，填入登录返回的 token 后，
 * 受保护接口会自动带请求头 token。</p>
 */
@Configuration
public class OpenApiConfiguration {

    /**
     * 定义全局的 token 请求头安全方案。
     *
     * @return OpenAPI 配置
     */
    @Bean
    public OpenAPI openAPI() {
        String schemeName = "token";
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("token");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes(schemeName, securityScheme))
                .addSecurityItem(new SecurityRequirement().addList(schemeName));
    }
}
