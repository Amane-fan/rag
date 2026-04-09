package com.usts.rag.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 应用启动入口。
 * <p>
 * 统一扫描所有业务模块中的 Spring 组件与配置属性，作为整个平台的装配根。
 */
@ConfigurationPropertiesScan("com.usts.rag")
@SpringBootApplication(scanBasePackages = "com.usts.rag")
public class RagApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagApplication.class, args);
    }
}
