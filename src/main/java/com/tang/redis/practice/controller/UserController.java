package com.tang.redis.practice.controller;

import com.tang.redis.practice.dto.Result;
import com.tang.redis.practice.vo.LoginFromVo;
import com.tang.redis.practice.dto.UserDto;
import com.tang.redis.practice.service.UserService;
import com.tang.redis.practice.util.UserHolder;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;

/**
 * @program: redis_heima
 * @description: 用户相关接口
 * @author: tang
 * @create: 2024-05-29 22:44
 **/
@RestController
@RequestMapping("user")
public class UserController {
     @Autowired
     private UserService userService;

     /**
      * 用户登录：后端根据邮箱/手机号和验证码（也可以用户名和密码）登录
      */
     @PostMapping("/code")
     public Result sendCode(@RequestParam("phone") String phone, HttpSession session) throws MessagingException {
          //发送短信验证码并保存验证码
          return userService.sendCode(phone, session);
     }


     /**
      * 发送手机验证码, 得到的验证码保存在session当中
      */
     @PostMapping("/login")
     public Result login(@RequestBody LoginFromVo loginFromVo, HttpSession session) {
          //发送短信验证码并保存验证码
          return userService.login(loginFromVo, session);
     }

     /**
      * 获取当前登录的用户的信息并返回：在请求的时候会调用
      */
     @GetMapping("/me")
     public Result me() {
          // 拦截器的时候已经放入了ThreadLocal中，所以从当前的ThreadLocal中去除即可
          UserDto userDto = UserHolder.getUser();
          return Result.ok(userDto);
     }

}
