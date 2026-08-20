package com.bookcycle.service;

import com.bookcycle.dto.social.HandleFriendRequest;
import com.bookcycle.dto.social.SendFriendRequest;
import com.bookcycle.entity.FriendRequest;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.FriendMapper;
import com.bookcycle.mapper.UserMapper;
import com.bookcycle.vo.BlacklistVO;
import com.bookcycle.vo.FriendRequestVO;
import com.bookcycle.vo.FriendVO;
import com.bookcycle.vo.UserBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 好友服务：搜索书友、好友申请（发送/同意/拒绝）、好友列表/删除、黑名单。
 * <p>黑名单双向阻断：任一方向拉黑则不能发送好友申请、不能交易（下单校验在 TradeService）。
 */
@Service
@RequiredArgsConstructor
public class FriendService {

    private static final int REQ_PENDING = 0;
    private static final int REQ_ACCEPTED = 1;
    private static final int REQ_REJECTED = 2;

    private final FriendMapper friendMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    /** 搜索书友（用户名/昵称模糊），空关键词返回空列表 */
    public List<UserBriefVO> search(String keyword, Long currentUserId) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return friendMapper.searchUsers(keyword.trim(), currentUserId);
    }

    /** 发送好友申请：幂等校验 + 黑名单阻断，成功通知接收方 */
    @Transactional
    public void sendRequest(SendFriendRequest req, Long fromUserId) {
        Long toUserId = req.getToUserId();
        if (toUserId.equals(fromUserId)) {
            throw new BusinessException("不能添加自己为好友");
        }
        if (userMapper.selectById(toUserId) == null) {
            throw new BusinessException("用户不存在");
        }
        if (friendMapper.countFriend(fromUserId, toUserId) > 0) {
            throw new BusinessException("你们已经是好友");
        }
        if (friendMapper.countBlackBetween(fromUserId, toUserId) > 0) {
            throw new BusinessException("双方存在拉黑关系，无法发送好友申请");
        }
        if (friendMapper.countPendingRequest(fromUserId, toUserId) > 0) {
            throw new BusinessException("好友申请已发送，等待对方处理");
        }
        FriendRequest r = new FriendRequest();
        r.setFromUserId(fromUserId);
        r.setToUserId(toUserId);
        friendMapper.insertRequest(r);
        UserBriefVO from = friendMapper.selectUserBrief(fromUserId);
        notificationService.notify(toUserId, NotificationService.TYPE_FRIEND,
                (from != null ? from.getNickname() : "对方") + " 请求添加你为好友");
    }

    /** 我收到的申请（含已处理历史） */
    public List<FriendRequestVO> listRequests(Long userId) {
        return friendMapper.selectRequests(userId);
    }

    /** 处理申请：accept 建双向好友 + 通知；reject 置拒绝 + 通知 */
    @Transactional
    public void handleRequest(Long id, HandleFriendRequest req, Long userId) {
        FriendRequest request = friendMapper.selectRequestById(id);
        if (request == null) {
            throw new BusinessException("申请不存在");
        }
        if (!request.getToUserId().equals(userId)) {
            throw new BusinessException(403, "无权处理该申请");
        }
        if (request.getStatus() != REQ_PENDING) {
            throw new BusinessException("该申请已处理");
        }
        if ("accept".equals(req.getAction())) {
            if (friendMapper.countBlackBetween(request.getFromUserId(), userId) > 0) {
                throw new BusinessException("双方存在拉黑关系，无法成为好友");
            }
            friendMapper.upsertFriend(request.getFromUserId(), userId);
            friendMapper.upsertFriend(userId, request.getFromUserId());
            friendMapper.updateRequestStatus(id, REQ_ACCEPTED);
            notificationService.notify(request.getFromUserId(), NotificationService.TYPE_FRIEND,
                    "对方已同意你的好友申请");
        } else if ("reject".equals(req.getAction())) {
            friendMapper.updateRequestStatus(id, REQ_REJECTED);
            notificationService.notify(request.getFromUserId(), NotificationService.TYPE_FRIEND,
                    "对方拒绝了你的好友申请");
        } else {
            throw new BusinessException("不支持的处理动作");
        }
    }

    public List<FriendVO> listFriends(Long userId) {
        return friendMapper.selectFriends(userId);
    }

    /** 删除好友：双向逻辑删除 */
    @Transactional
    public void deleteFriend(Long friendUserId, Long userId) {
        if (friendMapper.countFriend(userId, friendUserId) == 0) {
            throw new BusinessException("对方不是你的好友");
        }
        friendMapper.deleteFriendPair(userId, friendUserId);
    }

    public List<BlacklistVO> listBlacklist(Long userId) {
        return friendMapper.selectBlacklist(userId);
    }

    /** 拉黑（幂等），并自动解除双方好友关系；记录拉黑前是否为好友以便解除时恢复 */
    @Transactional
    public void addBlacklist(Long blackUserId, Long userId) {
        if (blackUserId.equals(userId)) {
            throw new BusinessException("不能拉黑自己");
        }
        if (userMapper.selectById(blackUserId) == null) {
            throw new BusinessException("用户不存在");
        }
        boolean wasFriend = friendMapper.countFriend(userId, blackUserId) > 0;
        friendMapper.insertBlacklist(userId, blackUserId, wasFriend);
        if (wasFriend) {
            friendMapper.deleteFriendPair(userId, blackUserId);
        }
    }

    /** 解除拉黑：拉黑前是好友且双方已无拉黑关系时，恢复双向好友 */
    @Transactional
    public void removeBlacklist(Long blackUserId, Long userId) {
        Integer wasFriend = friendMapper.selectBlackWasFriend(userId, blackUserId);
        friendMapper.deleteBlacklist(userId, blackUserId);
        if (wasFriend != null && wasFriend == 1 && friendMapper.countBlackBetween(userId, blackUserId) == 0) {
            friendMapper.upsertFriend(userId, blackUserId);
            friendMapper.upsertFriend(blackUserId, userId);
        }
    }
}
