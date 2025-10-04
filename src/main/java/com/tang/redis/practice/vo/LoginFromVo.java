package com.tang.redis.practice.vo;

import lombok.Data;

/**
 * @program: redis_heima
 * @description: 前端传入登录的对象
 * @author: tang
 * @create: 2024-06-08 10:05
 **/
@Data
public class LoginFromVo {
    private String phone;
    private String code;
    private String password;
}
