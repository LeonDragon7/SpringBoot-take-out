package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    /**
     * 分页查询
     * @param categoryPageQueryDTO
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 新增分类
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 启用禁用分类
     * @param status
     * @param id
     */
    void isOpen(Integer status, Long id);

    /**
     * 根据类型查询分类
     * @param type
     */
    List<Category> getByType(Integer type);

    /**
     * 修改分类
     * @param categoryDTO
     */
    void edit(CategoryDTO categoryDTO);

    /**
     * 删除分类
     * @param id
     */
    void removeById(Long id);
}
