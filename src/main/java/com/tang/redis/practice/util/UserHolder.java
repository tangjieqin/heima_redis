package com.tang.redis.practice.util;

import com.tang.redis.practice.dto.UserDto;

/**
 * @program: redis_heima
 * @description: 保存用户到ThreadLocal
 * @author: tang
 * @create: 2024-06-11 21:02
 **/
public class UserHolder {
    /**
     * 保存用户对象的ThreadLocal  在拦截器操作 添加、删除相关用户数据
     */
    private static final ThreadLocal<UserDto> tl = new ThreadLocal<>();
    public static void saveUser(UserDto user) {
        tl.set(user);
    }
    public static UserDto getUser() {
        return tl.get();
    }
    public static void removeUser() {
        tl.remove();
    }
}
