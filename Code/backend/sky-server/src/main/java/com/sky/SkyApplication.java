package com.sky;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 苍穹外卖后端启动类。
 *
 * <p>@MapperScan 指定 Mapper 接口所在包，这样 Mapper 接口不需要逐个加 @Mapper。</p>
 */
@SpringBootApplication
@MapperScan("com.sky.mapper")
public class SkyApplication {

    /**
     * 启动 Spring Boot 应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
    }
}
