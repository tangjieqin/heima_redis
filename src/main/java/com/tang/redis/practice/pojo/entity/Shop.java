package com.tang.redis.practice.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @program: redis_heima
 * @description: 商品
 * @author: tang
 * @create: 2024-07-02 18:54
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tb_shop")
public class Shop {
    private static final long serialVersionUID = 1L;

    /**
     *  主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 商铺名字
    private String name;

    // 商铺类型id
    private Long typeId;

    // 商铺图片，多个以，分割
    private String images;

    // 商圈
    private String area;

    // 地址
    private String address;

    // 经度
    private Double x;

    // 维度
    private Double y;

    // 均价，取整
    private Long avgPrice;

    // 销量
    private Integer sold;

    // 评论数量
    private Integer comments;

    // 评分：1~5分，乘10保存
    private Integer score;

    // 营业时间: 10:00-20:00
    private String openHours;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    @TableField(exist = false)  //不是数据库字段
    private Double distance;

}
