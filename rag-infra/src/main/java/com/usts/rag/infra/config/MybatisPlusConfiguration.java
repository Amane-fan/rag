package com.usts.rag.infra.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 基础配置，负责扫描 mapper 接口。
 */
@Configuration
@MapperScan("com.usts.rag.infra.persistence.mapper")
public class MybatisPlusConfiguration {
}
