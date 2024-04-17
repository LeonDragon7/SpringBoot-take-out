package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据openId获取用户信息
     * @param openId
     * @return
     */
    @Select("select * from user where openid = #{openId}")
    User getByOpenId(String openId);

    /**
     * 添加用户，并返回主键
     * @param user
     */
    void insert(User user);

    /**
     * 通过用户id查询用户
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 动态条件统计用户新增数量和总数量
     * @param map
     * @return
     */
    Integer countByMap(Map<String, Object> map);

}
