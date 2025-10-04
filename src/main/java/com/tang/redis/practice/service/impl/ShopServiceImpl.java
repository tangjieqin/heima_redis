package com.tang.redis.practice.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tang.redis.practice.dto.Result;
import com.tang.redis.practice.mapper.ShopMapper;
import com.tang.redis.practice.entity.Shop;
import com.tang.redis.practice.service.ShopService;
import com.tang.redis.practice.util.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
        * 补充：缓存更新一致性问题：数据库更新了，缓存也要更新
        * 添加超时剔除（防止数据长时间保留） + 主动更新（数据库更新，缓存也更新）
        * */

        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);  // 存在，转成对象，返回
            return Result.ok(shop);
        }
        // 不存在
        Shop shop = getById(id);
        if (shop == null) {
            return Result.fail( "店铺不存在!");
        }
        // 存Redis
        String jsonStr = JSONUtil.toJsonStr(shop);
        stringRedisTemplate.opsForValue().set(key, jsonStr, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);  // 时间是30分钟有效
        return Result.ok(shop);
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
}
