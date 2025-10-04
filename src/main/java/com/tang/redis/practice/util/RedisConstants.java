package com.tang.redis.practice.util;

/**
 * @program: redis_heima
 * @description: 变量
 * @author: tang
 * @create: 2024-06-14 18:47
 **/
public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final Long LOGIN_USER_TTL = 30L;
    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final String CACHE_SHOP_KEY = "cache:shop:";
}
