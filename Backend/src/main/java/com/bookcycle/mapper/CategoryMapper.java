package com.bookcycle.mapper;

import com.bookcycle.entity.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类 Mapper：简单 SQL 用注解，复杂/分页查询走 XML（resources/mapper/CategoryMapper.xml）。
 */
public interface CategoryMapper {

    @Select("SELECT id, parent_id, name, sort, status, create_time, update_time, deleted FROM category WHERE id = #{id} AND deleted = 0")
    Category selectById(@Param("id") Long id);

    /** 根分类列表（SQL 在 XML 中，供分页查询使用） */
    List<Category> selectRootList();
}
