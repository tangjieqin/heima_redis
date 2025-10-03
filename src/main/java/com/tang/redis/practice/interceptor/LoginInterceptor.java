package com.tang.redis.practice.interceptor;


import cn.hutool.core.bean.BeanUtil;
import com.tang.redis.practice.dto.UserDto;
import com.tang.redis.practice.pojo.entity.User;
import com.tang.redis.practice.util.RedisConstants;
import com.tang.redis.practice.util.UserHolder;
import io.micrometer.common.util.StringUtils;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.View;

import java.rmi.Remote;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    // 这个类不是IOC容器管理，自己创建的，所以不能依赖注入，但是可以在使用类，要想注入，可以用构造函数在使用这个类中注入
    private StringRedisTemplate stringRedisTemplate;
    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 请求前的拦截，对用户验证
        /*
        * 1. 获取请求头的token：从请求头中获取，前端代码会塞进authorization字段中
        * 2. 根据token获取redish中的用户的信息
        * 3. 将查询到的hashmap转成UserDto
        * 4. 用户的信息保存到ThreadLocal
        * 5. 刷新token的有效期
        * 6. 放行
        * */
        String token = request.getHeader("authorization");
        if (StringUtils.isBlank(token)) {
            // 不存在token,不拦截，返回401
            response.setStatus(401);
            return false;
        }

        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);
        if (userMap.isEmpty()) {
            // 用户不存在，不拦截，返回401
            response.setStatus(401);
            return false;
        }

        // 转成对象
        UserDto userDto = BeanUtil.fillBeanWithMap(userMap, new UserDto(), false);
        UserHolder.saveUser(userDto);

        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        return true;

       /* HttpSession session = request.getSession();
        // 这里的user是指登录成功之后，从数据库张获取的全部额用户信息，保存在session中
        // UserDTO：是前端传过来的数据，登录时保存的部分重要的用户的信息。
        // 但是为了避免存敏感的信息，我们在登录成功的时候，就将数据转成dto,存部分重要的数据。
        Object user = session.getAttribute("user");
        if (user == null) {
            // 用户不存在拦截，返回401
            response.setStatus(401);
            return false;
        }

        // 存在就保存用户的信息
        UserHolder.saveUser((UserDto) user);
        // 放行
        return true;*/
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, @Nullable Exception ex) throws Exception {
        // 移除用户,避免内存泄漏
        UserHolder.removeUser();
    }
}




























