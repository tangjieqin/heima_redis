package com.tang.redis.practice.util;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tang.redis.practice.entity.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // set,TTL
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    // 逻辑过期set
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        // 设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        redisData.setData(value);
        // 写入Redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    // 解决缓存穿透的方法：泛型来表示返回的类型，传入的参数来指明是什么泛型
    // 返回类型T， 传入的id类型是ID
    public <T, ID> T queryWithPassThrough(String keyPrefix, ID id, Long time, TimeUnit unit,
                                          Class<T> type, Function<ID, T> dbFallBack) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);  // 存在，转成对象，返回

        }
        // 添加如果是空值的，返回信息不存在
        if (json != null) {
            return null;
        }

        // 数据库查询判断：不知道查询哪个对象，所以只能传进来
        T t = dbFallBack.apply(id);
        if (t == null) {
            // 数据库也没有，缓存null到redis中
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 存Redis
        set(key, JSONUtil.toJsonStr(t), time, unit);
        return t;
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    // 缓存穿透：逻辑过期
    public  <T, ID> T queryWithLogicalExpire(String keyPrefix, ID id, Long time, TimeUnit unit,
                                             Class<T> type, Function<ID, T> dbFallback) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            // 缓存没查到，就直接返回旧的数据
            return null;
        }
        // 存在需要判断是否过期
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        T t = JSONUtil.toBean(data, type);
        LocalDateTime expireTime = redisData.getExpireTime();

        // 判断是否过期，当前时间之后表示没有过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 没有过期，直接返回
            return t;
        }

        // 过期：获取锁来创建新的线程去数据库查询来更新缓存
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        if (isLock) {
            // 再次检查缓存是否过期
            // 获取锁：创建独立的线程来更新数据：使用线程池
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 查询数据，放入缓存
                    T dbT = dbFallback.apply(id);
                    /*RedisData redisData2Redis = new RedisData();
                    redisData2Redis.setData(dbT);
                    redisData2Redis.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
                    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData2Redis));*/
                    this.setWithLogicalExpire(key,dbT,time,unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 重建之后需要释放锁
                    unlock(lockKey);
                }
            });
        }
        // 过期了，就直接返回过期的shop，异步
        return t;
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


}
