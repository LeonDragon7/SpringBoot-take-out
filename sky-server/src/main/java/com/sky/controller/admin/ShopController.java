package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminShopController")
@Api(tags = "店铺相关接口")
@RequestMapping(" /admin/shop")
public class ShopController {

    public static final String STATUS_KEY = "SHOP_STATUS";
    @Autowired
    private ShopService shopService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置营业状态
     * @param status
     * @return
     */
    @ApiOperation("设置营业状态")
    @PutMapping("/{status}")
    public Result status(@PathVariable Integer status){
        log.info("当前的营业状态:{}",status == 1 ? "营业中":"休息中");
        shopService.setStatus(status);
        return Result.success();
    }

    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(STATUS_KEY);
        log.info("当前的营业状态:{}",status == 1 ? "营业中":"休息中");
        return Result.success(status);
    }
}
