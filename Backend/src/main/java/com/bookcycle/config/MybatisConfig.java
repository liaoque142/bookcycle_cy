package com.bookcycle.config;

import com.github.pagehelper.PageInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * MyBatis 配置：PageHelper 分页拦截器。
 * <p>mybatis-spring-boot-starter 3.x 会自动把容器里的 Interceptor bean 挂到 MyBatis，
 * 因此这里只需声明 PageInterceptor（若再手动 addInterceptor 会重复注册导致 count 嵌套）。
 * PageHelper 的独立自动配置构件面向 Boot 2，本项目不引入。
 */
@Configuration
public class MybatisConfig {

    @Bean
    public PageInterceptor pageInterceptor(
            @Value("${pagehelper.helper-dialect:mysql}") String dialect,
            @Value("${pagehelper.reasonable:false}") boolean reasonable) {
        PageInterceptor interceptor = new PageInterceptor();
        Properties props = new Properties();
        props.setProperty("helperDialect", dialect);
        props.setProperty("reasonable", String.valueOf(reasonable));
        interceptor.setProperties(props);
        return interceptor;
    }
}
