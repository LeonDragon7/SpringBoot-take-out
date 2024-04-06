package com.sky.controller.admin;


import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api(tags = "分类相关接口")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @ApiOperation("分页查询")
    @GetMapping("/page")
    public Result<PageResult> index(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分类分页查询：{}",categoryPageQueryDTO);
        PageResult pageQuery = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageQuery);
    }

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @ApiOperation("新增分类")
    @PostMapping
    public Result save(@RequestBody CategoryDTO categoryDTO){
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     *
     * @param status 状态
     * @param id 分类id
     * @return
     */
    @ApiOperation("启用禁用分类")
    @PostMapping("/status/{status}")
    public Result status(@PathVariable Integer status,Long id){
        categoryService.isOpen(status,id);
        return Result.success();
    }

    /**
     * 根据类型查询分类
     * @param type 类型
     * @return
     */
    @ApiOperation("根据类型查询分类")
    @GetMapping("/list")
    public Result<List<Category>> getByType(Integer type){
        List<Category> category = categoryService.getByType(type);
        return Result.success(category);
    }

    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @ApiOperation("修改分类")
    @PutMapping
    public Result edit(@RequestBody CategoryDTO categoryDTO){
        categoryService.edit(categoryDTO);
        return Result.success();
    }

    /**
     * 删除分类
     * @param id
     * @return
     */
    @ApiOperation("删除分类")
    @DeleteMapping
    public Result delete(Long id){
        categoryService.removeById(id);
        return Result.success();
    }
}
