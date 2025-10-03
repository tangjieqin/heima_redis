package com.tang.redis.practice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tang.redis.practice.dto.Result;
import com.tang.redis.practice.mapper.UserMapper;
import com.tang.redis.practice.pojo.LoginFromVo;
import com.tang.redis.practice.dto.UserDto;
import com.tang.redis.practice.pojo.entity.User;
import com.tang.redis.practice.service.UserService;
import com.tang.redis.practice.util.MailUtils;
import com.tang.redis.practice.util.RedisConstants;
import com.tang.redis.practice.util.RegexUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @program: redis_heima
 * @description: 实现类
 * @author: tang
 * @create: 2024-06-01 10:09
 **/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) throws MessagingException {

        // houji
        if (!RegexUtils.isPhoneValid(phone)) {
            return Result.fail( "手机号码格式不正确！");
        }
//        String code = MailUtils.achieveCode();

        String code = RandomUtil.randomNumbers(6);


        // 验证码保存在Redis中，且有有效期,防止一直存，会存很多
//        session.setAttribute("code", code);

        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 5.发送验证码
        log.info("发送登录验证码：{}", code);
//        MailUtils.sendTestMail(phone, code);

        // 6.返回ok
        return Result.ok(null);
    }

    @Override
    public Result login(LoginFromVo loginFromVo, HttpSession session) {
        // 1.校验收手机号
        String phone = loginFromVo.getPhone();
        if (!RegexUtils.isPhoneValid(phone)) {
            return Result.fail("电话格式不正确！");
        }

//        Object sessionCode = session.getAttribute("code");

         // 2.从Redis中获取code
        String sessionCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String code = loginFromVo.getCode();
        if (sessionCode == null || !sessionCode.equals(code)) {
            return Result.fail("验证码错误！");
        }

        // 3.查询用户：根据手机号从数据库查询用户的信息
        User user = query().eq("phone", phone).one();

       /* LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(queryWrapper);*/

        if (user == null) {
            user = createUserWithPhone(phone);
        }

        /*// 将信息存入session,存入主要的信息，准成Dto
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDto.class));
        return Result.ok();*/

        /*
        * 保存用户的信息到Redis
        * 1. 随机生成token作为登录令牌，也作为存储user的key
        * 2. 将user对象转成hash存储,存储的时候要设置有效期i，防止数据过多
        * */
        String token = UUID.randomUUID().toString();
        UserDto userDto = BeanUtil.copyProperties(user, UserDto.class);
//        Map<String, Object> userMap = BeanUtil.beanToMap(userDto);
        // stringRedisTemplate 需要的全是String，所以要保证userMap中的key和value全是String才行
        // 两种方法：1. 用工具但是带有字段设计； 2. 手动获取添加new Map
        // 在用工具转化的时候可以设置字段的类型
        Map<String, Object> userMap = BeanUtil.beanToMap(userDto, new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true).
                        setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 设置有效期,并且需要更新，就是每次登录都更新一次有效期
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES); // 30分钟

        // 返回token, 前端会保存token消息axios
        return Result.ok(token);

        // UserDto转成Map,但是此时id是Long而不是String, edis中value是字符串，但是id是Long，需要转化
        /*HashMap<String, String> userMap = new HashMap<>();
        userMap.put("icon", userDto.getIcon());
        userMap.put("id", String.valueOf(userDto.getId()));
        userMap.put("nickName", userDto.getNickName());
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 4.3 设置token的有效保存时间是30分钟
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 4.4 登录成功以后需要删除验证码信息
        stringRedisTemplate.delete(RedisConstants.LOGIN_CODE_KEY + phone);
        // 5. 返回token给前端，前端存储后续放在请求头中
        log.info("token:{}",token);
        return Result.ok(token);*/
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_" + RandomUtil.randomString(8));
        // 保存到数据库
        // this.save(user);
        userMapper.insert(user);
        return user;
    }
}
