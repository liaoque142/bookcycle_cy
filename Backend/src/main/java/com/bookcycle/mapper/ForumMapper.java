package com.bookcycle.mapper;

import com.bookcycle.entity.ForumBoard;
import com.bookcycle.entity.ForumComment;
import com.bookcycle.entity.ForumPost;
import com.bookcycle.vo.ForumBoardVO;
import com.bookcycle.vo.ForumCommentVO;
import com.bookcycle.vo.ForumPostVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 论坛 Mapper：简单 SQL 用注解，联表/聚合查询走 XML（resources/mapper/ForumMapper.xml）。
 */
public interface ForumMapper {

    /** 启用板块列表（按 sort 升序） */
    @Select("SELECT id, name, description, sort FROM forum_board WHERE status = 1 AND deleted = 0 ORDER BY sort ASC, id ASC")
    List<ForumBoardVO> selectBoards();

    @Select("SELECT * FROM forum_board WHERE id = #{id} AND deleted = 0")
    ForumBoard selectBoardById(@Param("id") Long id);

    /** 帖子分页列表（板块/关键词过滤 + latest/hot 排序，聚合回复数，SQL 在 XML） */
    List<ForumPostVO> selectPostPage(@Param("boardId") Long boardId,
                                     @Param("keyword") String keyword,
                                     @Param("sort") String sort);

    /** 帖子详情（含当前用户是否已赞；currentUserId 可为 null，游客恒未赞） */
    ForumPostVO selectPostById(@Param("id") Long id, @Param("currentUserId") Long currentUserId);

    @Insert("INSERT INTO forum_post (board_id, user_id, title, content, like_count, view_count) " +
            "VALUES (#{boardId}, #{userId}, #{title}, #{content}, 0, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPost(ForumPost post);

    @Update("UPDATE forum_post SET board_id = #{boardId}, title = #{title}, content = #{content}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updatePost(ForumPost post);

    @Update("UPDATE forum_post SET deleted = 1, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int deletePost(@Param("id") Long id);

    @Update("UPDATE forum_post SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementView(@Param("id") Long id);

    /** 帖子评论列表（按时间升序） */
    List<ForumCommentVO> selectComments(@Param("postId") Long postId);

    @Insert("INSERT INTO forum_comment (post_id, user_id, content, image_url) VALUES (#{postId}, #{userId}, #{content}, #{imageUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertComment(ForumComment comment);

    @Select("SELECT COUNT(*) FROM forum_like WHERE post_id = #{postId} AND user_id = #{userId}")
    int countLike(@Param("postId") Long postId, @Param("userId") Long userId);

    @Insert("INSERT INTO forum_like (post_id, user_id) VALUES (#{postId}, #{userId})")
    int insertLike(@Param("postId") Long postId, @Param("userId") Long userId);

    @Delete("DELETE FROM forum_like WHERE post_id = #{postId} AND user_id = #{userId}")
    int deleteLike(@Param("postId") Long postId, @Param("userId") Long userId);

    @Update("UPDATE forum_post SET like_count = like_count + 1 WHERE id = #{postId}")
    int increaseLikeCount(@Param("postId") Long postId);

    @Update("UPDATE forum_post SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{postId}")
    int decreaseLikeCount(@Param("postId") Long postId);

    @Select("SELECT like_count FROM forum_post WHERE id = #{postId}")
    Integer selectLikeCount(@Param("postId") Long postId);
}
