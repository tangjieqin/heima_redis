package com.tang.redis.practice.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @program: redis_heima
 * @description: 登录用户的信息
 * @author: tang
 * @create: 2024-06-10 11:03
 **/
//一个对象序列化的接口，一个类只有实现了Serializable接口，它的对象才是可序列化的
@Data
public class User implements Serializable {
    // JAVA序列化的机制是通过 判断类的serialVersionUID来验证的版本一致的。
    private static final Long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id",  type = IdType.AUTO)
    private Long id;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 创建的时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 密码，加密存储
     */
    private String password;

    /**
     * 昵称，默认是随机字符
     */
    private String nickName;

    /**
     * 用户头像
     */
    private String icon = "";

}
