package com.tang.redis.practice.util;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @program: redis_heima
 * @description: 邮箱验证工具类,用于发送邮箱验证码
 * @author: tang
 * @create: 2024-06-02 19:46
 **/
public class MailUtils {

    public static void main(String[] args) throws MessagingException {
        // 这里可以直接测试
        sendTestMail("1099958947@qq.com", MailUtils.achieveCode());
    }

    public static void sendTestMail(String email, String code) throws MessagingException {
        // 创建Properties类，记录邮箱的属性
        Properties properties = new Properties();
        // 表示SMTP发送邮件必须进行身份验证
        properties.put("mail.smtp.auth", "true");
        // 填写SMTP服务器
        properties.put("mail.smtp.host", "smtp.qq.com");
        // 端口号，QQ邮箱端口号587
        properties.put("mail.smtp.port", "587");
        // 写信人的账号
        properties.put("mail.user", "1099958947@qq.com");
        // 16位SMTP口令
        properties.put("mail.password", "jsujvtddwaxhghag");
        // 构建授权信息，用于进行SMTP进行身份验证
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // 用户名、密码
                String username = properties.getProperty("mail.user");
                String password = properties.getProperty("mail.password");
                return new PasswordAuthentication(username, password);
            }
        };
        // 使用环境属性和授权信息，创建邮件会话
        Session mailSession = Session.getInstance(properties, authenticator);
        // 创建邮件消息
        MimeMessage mimeMessage = new MimeMessage(mailSession);
        // 设置发件人
        InternetAddress from = new InternetAddress(properties.getProperty("mail.user"));
        mimeMessage.setFrom(from);
        // 设置收件人的邮箱
        InternetAddress to = new InternetAddress(email);
        mimeMessage.setRecipient(MimeMessage.RecipientType.TO, to);
        // 设置邮件标题
        mimeMessage.setSubject("Redis的邮件测试");
        // 设置邮件的内容体
        mimeMessage.setContent("尊敬的用户：你好！\n注册验证码为：" + code + "(有效期为两分钟，请勿告知他人)", "text/html;charset=UTF-8");
        // 发送邮件
        Transport.send(mimeMessage);
    }

    public static String achieveCode() {
        //由于数字 1 、 0 和字母 O 、l 有时分不清楚，所以，没有数字 1 、 0
        String[] beforeShuffle = new String[]{"2", "3", "4", "5", "6", "7", "8", "9",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
                "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b",
                "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
                "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        // 将数组转成集合
        List<String> stringList = Arrays.asList(beforeShuffle);
        // 打乱集合元素顺序
        Collections.shuffle(stringList);
        // 集合转成字符串
        StringBuilder sb = new StringBuilder();
        for (String s : stringList) {
            sb.append(s);
        }
        return sb.substring(3, 8);
    }

}
