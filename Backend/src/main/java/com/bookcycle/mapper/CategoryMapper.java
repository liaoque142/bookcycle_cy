package com.bookcycle.mapper;

import com.bookcycle.entity.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 分类 Mapper：简单 SQL 用注解，复杂/分页查询走 XML（resources/mapper/CategoryMapper.xml）。
 */
public interface CategoryMapper {

    @Select("SELECT id, parent_id, name, sort, status, create_time, update_time, deleted FROM category WHERE id = #{id} AND deleted = 0")
    Category selectById(@Param("id") Long id);

    /** 根分类列表（SQL 在 XML 中，供分页查询使用） */
    List<Category> selectRootList();

    // ===== 管理后台 =====

    /** 全部分类（管理后台扁平列表，前端组树） */
    List<Category> selectAll();

    /** 同级同名分类计数（excludeId 可空：排除自身，用于编辑去重） */
    int countByNameAndParentExclude(@Param("name") String name, @Param("parentId") Long parentId, @Param("excludeId") Long excludeId);

    /** 子分类计数（删除前校验） */
    @Select("SELECT COUNT(*) FROM category WHERE parent_id = #{parentId} AND deleted = 0")
    int countChildren(@Param("parentId") Long parentId);

    @Insert("INSERT INTO category (parent_id, name, sort, status) VALUES (#{parentId}, #{name}, #{sort}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE category SET name = #{name}, parent_id = #{parentId}, sort = #{sort}, status = #{status} WHERE id = #{id}")
    int update(Category category);

    /** 逻辑删除 */
    @Update("UPDATE category SET deleted = 1 WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
