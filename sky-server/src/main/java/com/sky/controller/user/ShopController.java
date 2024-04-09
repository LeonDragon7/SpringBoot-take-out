package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userShopController")
@Api(tags = "店铺相关接口")
@RequestMapping(" /user/shop")
public class ShopController {
    public static final String STATUS_KEY = "SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(STATUS_KEY);
        log.info("当前的营业状态:{}",status == 1 ? "营业中":"休息中");
        return Result.success(status);
    }
}
