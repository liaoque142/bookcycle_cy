package com.bookcycle.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 收藏 Mapper：单行写用注解（uk_user_book 幂等），分页查询在 BookMapper.xml 复用 bookVoColumns。
 */
public interface FavoriteMapper {

    /** 收藏（INSERT IGNORE：重复收藏返回 0，幂等） */
    @Insert("INSERT IGNORE INTO favorite (user_id, book_id) VALUES (#{userId}, #{bookId})")
    int insert(@Param("userId") Long userId, @Param("bookId") Long bookId);

    /** 取消收藏（0 行不报错） */
    @Delete("DELETE FROM favorite WHERE user_id = #{userId} AND book_id = #{bookId}")
    int deleteByUserBook(@Param("userId") Long userId, @Param("bookId") Long bookId);
}
