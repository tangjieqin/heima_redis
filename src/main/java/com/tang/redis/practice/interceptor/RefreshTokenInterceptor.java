package com.tang.redis.practice.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.tang.redis.practice.dto.UserDto;
import com.tang.redis.practice.util.RedisConstants;
import com.tang.redis.practice.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @program: redis_heima
 * @description: 拦截所有的路径，刷新缓存和token
 * @author: tang
 * @create: 2024-06-25 10:05
 **/
@Slf4j
public class RefreshTokenInterceptor implements HandlerInterceptor {

    // 使用时是从config配置类注入进来的
    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("RefreshTokenInterceptor start!");
        // 1.请求头中获取token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            // 为空，不用拦截，直接放行
            return true;
        }
        // 2.获取Redis中用户的信息:get是获取某个map的某个key的value
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(RedisConstants.LOGIN_USER_KEY + token);
        // 判断用户是否存在
        if (userMap.isEmpty()) {
            // 不存在也不拦截，直接放行
            return true;
        }
        // 刷新用户的和token的信息：只要访问就刷新
        // Map转成UserDto, 注意id是Long而Redis中是String
        UserDto userDto = BeanUtil.fillBeanWithMap(userMap, new UserDto(), false);
        // 3.将用户保存在ThreadLocal中，UserHolder是提供好了的工具类
        UserHolder.saveUser(userDto);
        // 4.每次请求都刷新一次token的有效期
        stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY + token, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        log.info("userDto:{}", userDto);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)  {
        // 移除用户，避免线程泄露
        UserHolder.removeUser();
    }
}
