package com.tang.redis.practice.service.impl;


import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tang.redis.practice.dto.Result;
import com.tang.redis.practice.entity.RedisData;
import com.tang.redis.practice.mapper.ShopMapper;
import com.tang.redis.practice.entity.Shop;
import com.tang.redis.practice.service.ShopService;
import com.tang.redis.practice.util.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @program: redis_heima
 * @description: 商户的业务层
 * @author: tang
 * @create: 2024-06-29 09:58
 **/
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        /*
        * 1. 先从Redis缓存中查询，通过key=固定的前缀+id
        * 2. 判断是否存在，不存在从, 根据id数据库取，并放入缓存redis,存在直接返回
        * 3. 不存在，根据id查询数据库(mybatisPlus)：转成Json放入缓存
        * */

        /*
        * 解决缓存穿透：
        * 补充：缓存更新一致性问题：数据库更新了，缓存也要更新
        * 添加超时剔除（防止数据长时间保留） + 主动更新（数据库更新，缓存也更新）
        * */

        /*
            解决缓存击穿：
            1. 互斥锁
            2. 逻辑过去
         */

        // 缓存穿透
//        Shop shop = queryWithPassThrough(id);

        // 缓存穿透：互斥锁
//        Shop shop = queryWithMutex(id);

        Shop shop = queryWithLogicalExpire(id);
        if (shop == null) {
            return Result.fail("店铺不存在");
        }

        // 缓存穿透：逻辑过期
        return Result.ok(shop);
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    // 缓存穿透：逻辑过期
    private Shop queryWithLogicalExpire(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(shopJson)) {
            // 缓存没查到，就直接返回旧的数据
            return null;
        }
        // 存在需要判断是否过期
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        Shop shop = JSONUtil.toBean(data, Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();

        // 判断是否过期，当前时间之后表示没有过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 没有过期，直接返回
            return shop;
        }

        // 过期：获取锁来创建新的线程去数据库查询来更新缓存
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        if (isLock) {
            // 再次检查缓存是否过期
            // 获取锁：创建独立的线程来更新数据：使用线程池
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 重建缓存
                    this.saveShop2Redis(id,30 * 60L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 重建之后需要释放锁
                    unlock(lockKey);
                }
            });
        }
        // 过期了，就直接返回过期的shop，异步
        return shop;
    }


    // 缓存穿透：互斥锁
    private Shop queryWithMutex(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);  // 存在，转成对象，返回
            return shop;
        }
        // null也返回，缓存穿透
        if (shopJson != null) {
            return null;
        }
        /*
        缓存击穿：互斥锁
        1. 获取互斥锁， 失败休眠一段时间，继续重试获取
        2. 获取锁，根据id查询数据库
         */
        // 互斥锁：每个商铺id有自己的锁，不能和缓存的key混为一谈
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        try {
            boolean isLock = tryLock(lockKey);
            if (!isLock) {
                // 获取失败，休息一段时间，继续获取（递归）
                Thread.sleep(50);
                return queryWithMutex(id);
            }
            // 成功，去数据库查询
            Shop shop = getById(id);
            // 模拟查询数据阻塞
            Thread.sleep(200);
            if (shop == null) {
                // 数据库不存在，redis中存null
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
                return null;
            }
            // 存在，写入redis
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
            return shop;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 释放锁
            unlock(lockKey);
        }

    }

    private void unlock(String lockKey) {
        stringRedisTemplate.delete(lockKey);
    }

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁的键
     * @return 是否获取成功
     */
    private boolean tryLock(String lockKey) {
        // 锁的值、锁的过期时间
        Boolean isLock = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(isLock);
    }


    // 解决缓存穿透的方法
    private Shop queryWithPassThrough(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);  // 存在，转成对象，返回
            return shop;
        }
        // 添加如果是空值的，返回信息不存在
        if (shopJson != null) {
            return null;
        }
        // 数据库查询判断
        Shop shop = getById(id);
        if (shop == null) {
            // 数据库也没有，缓存null到redis中
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 存Redis
        String jsonStr = JSONUtil.toJsonStr(shop);
        stringRedisTemplate.opsForValue().set(key, jsonStr, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);  // 时间是30分钟有效
        return shop;
    }

    @Override
    public Result updateShopById(Shop shop) {
        /*
        1. 更新数据库
        2. 删除缓存
         */
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id为空");
        }
        updateById(shop);
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    // 数据预热，提前将数据存入缓存的操作
    private void saveShop2Redis(Long id, Long expireSeconds) {
        /*
        1. 从数据库查询
        2. 重新封装成带有过期时间的数据。逻辑过期时间
        3. 写入redis
         */
        Shop shop = getById(id);
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        // 存入缓存的时候就不需要TTL了。
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }
}
