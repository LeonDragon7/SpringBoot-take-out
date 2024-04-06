package com.sky.service;

import com.github.pagehelper.IPage;
import com.github.pagehelper.Page;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO 前端员工数据
     * @return
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param queryDTO
     * @return
     */
    PageResult pageList(EmployeePageQueryDTO queryDTO);

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    void status(Integer status,Long id);

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    Employee getId(Long id);

    /**
     * 编辑员工信息
     * @param employeeDTO
     * @return
     */
    void edit(EmployeeDTO employeeDTO);
}
