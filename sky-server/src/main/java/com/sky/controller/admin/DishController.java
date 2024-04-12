package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Api(tags = "菜品相关接口")
@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        dishService.insertWithFlavor(dishDTO);
        //清理缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    /**
     * 分页查询
     * @param queryDTO
     * @return
     */
    @ApiOperation("菜品分页查询")
    @GetMapping("page")
    public Result<PageResult> page(DishPageQueryDTO queryDTO){
        PageResult pagedQuery = dishService.pageQuery(queryDTO);
        return Result.success(pagedQuery);
    }


    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @ApiOperation("删除菜品")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品删除：{}",ids);
        dishService.delete(ids);
        //将所有的菜品缓存数据清除,以dish_开头的key
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @ApiOperation("根据id查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
        DishVO dish = dishService.getByIdWithFlavor(id);
        return Result.success(dish);
    }

    /**
     * 编辑菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("编辑菜品")
    @PutMapping
    public Result edit(@RequestBody DishDTO dishDTO){
        log.info("菜品参数：{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        //将所有的菜品缓存数据清除,以dish_开头的key
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 清理缓存数据
     * @param pattern
     */
    private void cleanCache(String  pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
