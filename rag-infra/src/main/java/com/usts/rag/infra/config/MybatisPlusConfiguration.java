package com.usts.rag.infra.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.usts.rag.infra.persistence.mapper")
public class MybatisPlusConfiguration {
}
