package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    void insertWithFlavor(DishDTO dishDTO);

    /**
     * 分页查询
     * @param queryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO queryDTO);

    /**
     * 删除菜品
     * @param ids
     */
    void delete(List<Long> ids);
}
