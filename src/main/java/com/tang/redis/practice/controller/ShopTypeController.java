package com.tang.redis.practice.controller;

import com.tang.redis.practice.dto.Result;
import com.tang.redis.practice.entity.ShopType;
import com.tang.redis.practice.service.IShopTypeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Resource
    private IShopTypeService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
        List<ShopType> typeList = typeService.query().orderByAsc("sort").list();
        return Result.ok(typeList);
    }
}
