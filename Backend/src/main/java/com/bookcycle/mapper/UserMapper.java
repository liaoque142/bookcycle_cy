package com.bookcycle.mapper;

import com.bookcycle.entity.SysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 用户 Mapper：认证相关简单 SQL 用注解，复杂/分页查询走 XML。
 */
public interface UserMapper {

    @Select("SELECT * FROM sys_user WHERE phone = #{phone} AND deleted = 0")
    SysUser selectByPhone(String phone);

    @Select("SELECT * FROM sys_user WHERE email = #{email} AND deleted = 0")
    SysUser selectByEmail(String email);

    @Select("SELECT * FROM sys_user WHERE id = #{id} AND deleted = 0")
    SysUser selectById(@Param("id") Long id);

    @Insert("INSERT INTO sys_user (username, password, phone, email, credit, status, role_id) " +
            "VALUES (#{username}, #{password}, #{phone}, #{email}, #{credit}, #{status}, #{roleId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser user);

    @Update("UPDATE sys_user SET last_login_time = #{time} WHERE id = #{id}")
    int updateLastLoginTime(@Param("id") Long id, @Param("time") LocalDateTime time);
}
