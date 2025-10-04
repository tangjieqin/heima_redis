package com.tang.redis.practice.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.tang.redis.practice.dto.Result;
import com.tang.redis.practice.entity.Shop;

public interface ShopService extends IService<Shop> {

    Result queryById(Long id);
}
