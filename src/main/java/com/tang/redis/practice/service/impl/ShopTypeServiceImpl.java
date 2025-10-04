package com.tang.redis.practice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tang.redis.practice.entity.ShopType;
import com.tang.redis.practice.mapper.ShopTypeMapper;
import com.tang.redis.practice.service.IShopTypeService;
import org.springframework.stereotype.Service;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
}
