package com.sky.service.impl;

import com.sky.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ShopServiceImpl implements ShopService {
    public static final String STATUS_KEY = "SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 设置营业状态
     * @param status
     */
    @Override
    public void setStatus(Integer status) {
        //将状态值保存到redis
        redisTemplate.opsForValue().set(STATUS_KEY,status,12, TimeUnit.HOURS);
    }
}
