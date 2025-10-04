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

        String shopJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);

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
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, jsonStr);
        return Result.ok(shop);
    }
}
