package com.bookcycle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BookCycle（书循环）启动类。
 */
@SpringBootApplication
@MapperScan("com.bookcycle.mapper")
public class BookCycleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookCycleApplication.class, args);
    }
}
