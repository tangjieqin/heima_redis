package com.tang.redis.practice.entity;

import lombok.Data;

import java.time.LocalDateTime;

/*
逻辑过期，重新定义一个类
 */
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
