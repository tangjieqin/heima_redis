package com.tang.redis.practice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tang.redis.practice.dto.Result;
import com.tang.redis.practice.vo.LoginFromVo;
import com.tang.redis.practice.entity.User;
import jakarta.servlet.http.HttpSession;

import javax.mail.MessagingException;

/**
 * @program: redis_heima
 * @description: 实现方法
 * @author: tang
 * @create: 2024-06-01 10:06
 **/

public interface UserService extends IService<User> {

    Result sendCode(String phone, HttpSession session) throws MessagingException;

    Result login(LoginFromVo loginFromVo, HttpSession session);
}
