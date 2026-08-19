package com.bookcycle.service;

import com.bookcycle.dto.social.SendMessageRequest;
import com.bookcycle.entity.Message;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.FriendMapper;
import com.bookcycle.mapper.MessageMapper;
import com.bookcycle.mapper.UserMapper;
import com.bookcycle.vo.MessageVO;
import com.bookcycle.vo.PeerUnreadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 私信服务：好友间一对一消息，仅双方可见可回复，第三者不可见。
 * <p>发送仅限好友（黑名单已自动解除好友，天然阻断被拉黑方）。
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private static final int HISTORY_LIMIT = 100;

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final FriendMapper friendMapper;

    /** 发送私信：不能发给自己 / 用户须存在 / 仅好友可发 */
    @Transactional
    public void send(SendMessageRequest req, Long fromUserId) {
        Long toUserId = req.getToUserId();
        if (toUserId.equals(fromUserId)) {
            throw new BusinessException("不能给自己发送私信");
        }
        if (userMapper.selectById(toUserId) == null) {
            throw new BusinessException("用户不存在");
        }
        if (friendMapper.countFriend(fromUserId, toUserId) == 0) {
            throw new BusinessException("只能给好友发送私信");
        }
        Message m = new Message();
        m.setSenderId(fromUserId);
        m.setReceiverId(toUserId);
        m.setContent(req.getContent().trim());
        messageMapper.insert(m);
    }

    /** 与某好友的聊天记录（最近 100 条正序）；查询以本人为边界，第三者访问为空 */
    public List<MessageVO> history(Long peerId, Long me) {
        return messageMapper.selectHistory(me, peerId, HISTORY_LIMIT);
    }

    /** 标记该好友发来的全部消息已读 */
    public void markPeerRead(Long peerId, Long me) {
        messageMapper.markPeerRead(peerId, me);
    }

    /** 全部未读总数（Header 红点） */
    public int unreadCount(Long me) {
        return messageMapper.countUnread(me);
    }

    /** 按好友未读数（好友行角标） */
    public List<PeerUnreadVO> unreadSummary(Long me) {
        return messageMapper.selectUnreadSummary(me);
    }
}
