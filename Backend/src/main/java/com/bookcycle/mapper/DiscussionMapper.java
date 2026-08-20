package com.bookcycle.mapper;

import com.bookcycle.entity.BookDiscussion;
import com.bookcycle.vo.DiscussionVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 书籍讨论 Mapper：列表联表取昵称走 XML（resources/mapper/DiscussionMapper.xml）。
 */
public interface DiscussionMapper {

    /** 顶级讨论（parent_id IS NULL，时间倒序） */
    List<DiscussionVO> selectTop(@Param("bookId") Long bookId);

    /** 全部子回复（parent_id 非空，时间正序，Service 端按 parentId 组装） */
    List<DiscussionVO> selectReplies(@Param("bookId") Long bookId);

    @Insert("INSERT INTO book_discussion (book_id, user_id, content, image_url, parent_id) VALUES (#{bookId}, #{userId}, #{content}, #{imageUrl}, #{parentId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BookDiscussion discussion);

    @Select("SELECT * FROM book_discussion WHERE id = #{id} AND deleted = 0")
    BookDiscussion selectById(@Param("id") Long id);

    @Update("UPDATE book_discussion SET deleted = 1, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int delete(@Param("id") Long id);
}
