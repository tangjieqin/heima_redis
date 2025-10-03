package com.tang.redis.practice.util;

import cn.hutool.core.util.StrUtil;

/**
 * @program: redis_heima
 * @description: 利用正则表达式校验
 * @author: tang
 * @create: 2024-06-01 10:52
 **/
public class RegexUtils {
    /**
     * 是否是有效手机格式
     */
    public static boolean isPhoneValid(String phone) {
        return isMatch(phone, RegexPatterns.PHONE_REGEX);
    }

    /**
     * 检验邮箱格式是否正确
     */
    public static boolean isEmailValid(String email) {
        return isMatch(email, RegexPatterns.EMAIL_REGEX);
    }

    /**
     * 检验验证码是否格式正确
     */
    public static boolean isCodeValid(String code) {
        return isMatch(code, RegexPatterns.VERIFY_CODE_REGEX);
    }

    /**
     * 检验密码格式是否正确
     */
    public static boolean isPasswordValid(String password) {
        return isMatch(password, RegexPatterns.PASSWORD_REGEX);
    }


    /**
     * 校验正则表达式是否匹配
     */
    private static boolean isMatch(String str, String regex) {
        if (StrUtil.isBlank(str)) {
            return false;
        }
        return str.matches(regex);
    }


}
