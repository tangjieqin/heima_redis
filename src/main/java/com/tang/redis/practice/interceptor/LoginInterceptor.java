package com.tang.redis.practice.interceptor;

import com.tang.redis.practice.dto.UserDto;
import com.tang.redis.practice.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.servlet.HandlerInterceptor;


/**
 * @program: redis_heima
 * @description: 登录验证拦截器
 * 拦截器：1，访问接口时将用户信息放入ThreadLocal，2，访问结束时候删除ThreadLocal中信息（线程放入线程池并不一定会销毁）
 *
 * ThreadLocal对应的是一个线程的数据，每次http请求，tomcat都会创建一个新的线程，ThreaLocal底层是ThreadLocalMap,当前线程作为key（弱引用）,Value是user(强引用），Jvm不会把强引用回收，所以Value没有释放
 *
 * 弱引用，自动垃圾回收。
 * 强引用，线程销毁时，才会被回收。
 * 一个线程可能有时候很久都不会被销毁，但是这时候只有弱引用的 key 会被回收，value 由于是强引用，由于线程还存在，他就会存在，但是 value 已经没有用了，这个时候就造成了浪费。
 * 为了避免浪费内存，继而发生内存溢出，我们需要使用 remove() 方法，进行手动清除 ThreadLocal 对象。
 *
 * 所以我通过 ThreadLocal ，将其与当前线程绑定，当前整个请求结束再去清理内存，下一次请求重新分配。
 *
 * @author: tang
 * @create: 2024-06-10 21:21
 **/
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 这个第二层的拦截只需要查看这个ThreadLocal 中是否有用户
        UserDto user = UserHolder.getUser();
        if (user == null) {
            // 用户不存在拦截，返回401
            response.setStatus(401);
            return false;
        }
        return true;
    }
}




























