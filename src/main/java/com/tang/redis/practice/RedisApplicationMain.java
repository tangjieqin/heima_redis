package com.tang.redis.practice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: redis_heima
 * @description: 启动类
 * @author: tang
 * @create: 2024-06-02 09:03
 **/
@SpringBootApplication
@MapperScan("com.tang.redis.practice.mapper")
public class RedisApplicationMain {
    public static void main(String[] args) {
        SpringApplication.run(RedisApplicationMain.class, args);
    }
}
