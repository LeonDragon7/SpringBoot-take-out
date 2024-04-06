package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "菜品相关接口")
@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        dishService.insertWithFlavor(dishDTO);
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
        return Result.success();
    }
}
