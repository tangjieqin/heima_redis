package com.tang.redis.practice.dto;

import lombok.Data;

/**
 * @program: redis_heima
 * @description: 后端返回的用户信息
 * @author: tang
 * @create: 2024-06-09 16:45
 **/
@Data
public class UserDto {
    private Long id;
    private String nickName;
    private String icon;
}
