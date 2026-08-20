package com.bookcycle.mapper;

import com.bookcycle.entity.Message;
import com.bookcycle.vo.MessageVO;
import com.bookcycle.vo.PeerUnreadVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 私信 Mapper：简单 SQL 走注解，两人历史走 XML（子查询取最近 N 条再正序）。
 * <p>所有查询均以当前用户为边界，第三者无法读到他人会话。
 */
public interface MessageMapper {

    @Insert("INSERT INTO message (sender_id, receiver_id, content, image_url, is_read) VALUES (#{senderId}, #{receiverId}, #{content}, #{imageUrl}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);

    /** 两人最近 limit 条消息（正序），仅参与者可见 */
    List<MessageVO> selectHistory(@Param("me") Long me, @Param("peer") Long peer, @Param("limit") int limit);

    /** 标记该好友发来的全部消息已读 */
    @Update("UPDATE message SET is_read = 1 WHERE receiver_id = #{me} AND sender_id = #{peer} AND is_read = 0")
    int markPeerRead(@Param("peer") Long peer, @Param("me") Long me);

    /** 全部未读总数（Header 红点） */
    @Select("SELECT COUNT(*) FROM message WHERE receiver_id = #{me} AND is_read = 0")
    int countUnread(@Param("me") Long me);

    /** 按好友未读数（好友行角标） */
    @Select("SELECT sender_id AS userId, COUNT(*) AS unreadCount FROM message WHERE receiver_id = #{me} AND is_read = 0 GROUP BY sender_id")
    List<PeerUnreadVO> selectUnreadSummary(@Param("me") Long me);

    /** 消息中心分页列表：我收到的全部消息（跨好友，含发送者昵称/头像，SQL 在 XML） */
    List<MessageVO> selectInboxPage(@Param("me") Long me);

    /** 标记单条已读：仅接收者本人可标（防越权），已读幂等 */
    @Update("UPDATE message SET is_read = 1 WHERE id = #{id} AND receiver_id = #{me} AND is_read = 0")
    int markSingleRead(@Param("id") Long id, @Param("me") Long me);
}
