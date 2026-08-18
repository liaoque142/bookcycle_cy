package com.bookcycle.mapper;

import com.bookcycle.entity.FriendRequest;
import com.bookcycle.vo.BlacklistVO;
import com.bookcycle.vo.FriendRequestVO;
import com.bookcycle.vo.FriendVO;
import com.bookcycle.vo.UserBriefVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 社交 Mapper：好友 / 申请 / 黑名单 / 用户搜索，联表查询走 XML（resources/mapper/FriendMapper.xml）。
 */
public interface FriendMapper {

    /** 搜索启用状态的用户（排除自己，昵称/用户名模糊，最多 20 条） */
    List<UserBriefVO> searchUsers(@Param("keyword") String keyword, @Param("currentUserId") Long currentUserId);

    /** 单个用户摘要（昵称缺省回退用户名），用于好友申请通知 */
    UserBriefVO selectUserBrief(@Param("id") Long id);

    /** 我的好友列表（friend 表当前用户一行，取好友侧信息） */
    List<FriendVO> selectFriends(@Param("userId") Long userId);

    /** 两人是否已是好友（双向任一 status=1） */
    int countFriend(@Param("a") Long a, @Param("b") Long b);

    /** from → to 是否已有待处理申请 */
    int countPendingRequest(@Param("from") Long from, @Param("to") Long to);

    @Insert("INSERT INTO friend_request (from_user_id, to_user_id, status) VALUES (#{fromUserId}, #{toUserId}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRequest(FriendRequest request);

    @Select("SELECT * FROM friend_request WHERE id = #{id}")
    FriendRequest selectRequestById(@Param("id") Long id);

    /** 我收到的申请（含申请人信息，按时间倒序） */
    List<FriendRequestVO> selectRequests(@Param("toUserId") Long toUserId);

    @Update("UPDATE friend_request SET status = #{status} WHERE id = #{id}")
    int updateRequestStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 建立双向好友关系：已存在旧行（status=0）则恢复为 1，否则新增 */
    int upsertFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /** 删除好友：双向 status 置 0（逻辑删除） */
    int deleteFriendPair(@Param("a") Long a, @Param("b") Long b);

    /** 单向拉黑是否已存在 */
    int countBlack(@Param("userId") Long userId, @Param("blackUserId") Long blackUserId);

    /** 任一方向存在拉黑即返回 >0 */
    int countBlackBetween(@Param("a") Long a, @Param("b") Long b);

    /** 拉黑（INSERT IGNORE 幂等） */
    @Insert("INSERT IGNORE INTO blacklist (user_id, black_user_id) VALUES (#{userId}, #{blackUserId})")
    int insertBlacklist(@Param("userId") Long userId, @Param("blackUserId") Long blackUserId);

    /** 解除拉黑 */
    @Delete("DELETE FROM blacklist WHERE user_id = #{userId} AND black_user_id = #{blackUserId}")
    int deleteBlacklist(@Param("userId") Long userId, @Param("blackUserId") Long blackUserId);

    /** 我的黑名单（含被拉黑用户信息） */
    List<BlacklistVO> selectBlacklist(@Param("userId") Long userId);
}
