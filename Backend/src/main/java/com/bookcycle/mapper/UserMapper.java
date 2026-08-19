package com.bookcycle.mapper;

import com.bookcycle.entity.SysUser;
import com.bookcycle.vo.AdminUserVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户 Mapper：认证相关简单 SQL 用注解，复杂/分页查询走 XML（resources/mapper/UserMapper.xml）。
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

    // ===== 管理后台 =====

    /** 管理后台用户分页列表（关键词/状态筛选，SQL 在 XML） */
    List<AdminUserVO> selectAdminPage(@Param("keyword") String keyword, @Param("status") Integer status);

    /** 管理后台用户详情 */
    AdminUserVO selectAdminDetail(@Param("id") Long id);

    /** 启用/禁用用户 */
    @Update("UPDATE sys_user SET status = #{status} WHERE id = #{id} AND deleted = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 查询用户角色编码（用于禁止禁用管理员账号） */
    @Select("SELECT r.role_code FROM role r JOIN sys_user su ON su.role_id = r.id WHERE su.id = #{userId} AND su.deleted = 0")
    String selectRoleCodeByUserId(@Param("userId") Long userId);
}
