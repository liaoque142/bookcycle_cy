package com.bookcycle.mapper;

import com.bookcycle.entity.Role;
import org.apache.ibatis.annotations.Select;

/**
 * 角色 Mapper。
 */
public interface RoleMapper {

    @Select("SELECT * FROM role WHERE id = #{id}")
    Role selectById(Long id);

    @Select("SELECT * FROM role WHERE role_code = #{roleCode}")
    Role selectByCode(String roleCode);
}
