package com.tang.redis.practice.controller;

import com.tang.redis.practice.dto.Result;
import com.tang.redis.practice.entity.Shop;
import com.tang.redis.practice.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @program: redis_heima
 * @description: 商户的控制层
 * @author: tang
 * @create: 2024-06-29 09:51
 **/
@RestController
@RequestMapping("shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    /**
     * 根据id查询商铺信息
     * @param id 商铺id
     * @return 商铺详情数据
     */
    @GetMapping("/{id}")
    public Result queryShopById(@PathVariable("id") Long id) {
        return shopService.queryById(id);
    }

    /**
     * 更新商铺的信息
     */
    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        // 写入数据
        return shopService.updateShopById(shop);
    }


}
