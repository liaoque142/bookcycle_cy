package com.bookcycle.mapper;

import com.bookcycle.entity.Notification;
import com.bookcycle.vo.NotificationVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 通知 Mapper：分页列表走 XML（resources/mapper/NotificationMapper.xml），其余简单 SQL 注解。
 */
public interface NotificationMapper {

    @Insert("INSERT INTO notification (user_id, type, content, is_read) VALUES (#{userId}, #{type}, #{content}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);

    /** 通知分页列表（unreadOnly=true 仅未读；typeName 中文在 XML CASE 映射） */
    List<NotificationVO> selectPage(@Param("userId") Long userId, @Param("unreadOnly") Boolean unreadOnly);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    int selectUnreadCount(@Param("userId") Long userId);

    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllRead(@Param("userId") Long userId);
}
