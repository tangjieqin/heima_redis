package com.tang.redis.practice.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tang.redis.practice.dto.Result;
import com.tang.redis.practice.mapper.ShopMapper;
import com.tang.redis.practice.pojo.entity.Shop;
import com.tang.redis.practice.service.ShopService;
import com.tang.redis.practice.util.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @program: redis_heima
 * @description: 商户的业务层
 * @author: tang
 * @create: 2024-06-29 09:58
 **/
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryById(Long id) {
        // 先从Redis缓存中查询，通过key=固定的前缀+id
        String shopJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);

        // 判断是否存在，不存在从数据库取，并放入缓存
        // 存在直接返回
        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        // 不存在，根据id查询数据库(mybatisPlus)
        Shop shop = getById(id);
        if (shop == null) {
            return Result.fail( "店铺不存在!");
        }
        // 转成Json放入缓存
        String jsonStr = JSONUtil.toJsonStr(shop);
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, jsonStr);
        return Result.ok(shop);
    }
}
