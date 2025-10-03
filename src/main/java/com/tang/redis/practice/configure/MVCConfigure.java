package com.tang.redis.practice.configure;

import com.tang.redis.practice.interceptor.LoginInterceptor;
import com.tang.redis.practice.interceptor.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @program: redis_heima
 * @description: 拦截器的配置类
 * @author: tang
 * @create: 2024-06-11 21:30
 **/
@Configuration
public class MVCConfigure implements WebMvcConfigurer {

    // 在这里注入是为了得到最新的stringRedisTemplate bean，只有这里使用了LoginInterceptor
    // 这样就可以将stringRedisTemplate，注入到LoginInterceptor中了
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // registry注册器，然后添加需要拦截的路径,或者排除哪些拦截的路径
        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate)).excludePathPatterns(
                "/user/code",
                "/user/login",
                "/blog/hot",
                "/shop/**",
                "/shop-type/**",
                "/upload/**",
                "/voucher/**").order(1);

        // 默认是拦截所有“/**”,该拦截器应该需要先执行:默认是按照添加顺序执行，也可以使用order，越小越先执行
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
    }
}
