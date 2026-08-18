package com.bookcycle.service;

import com.bookcycle.entity.Notification;
import com.bookcycle.common.PageResult;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.NotificationMapper;
import com.bookcycle.vo.NotificationVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知服务：写入站内信，供交易 / 好友等模块调用；查询分页 + 未读数 + 已读。
 * <p>type：1 系统 / 2 交易 / 3 好友。
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_TRADE = 2;
    public static final int TYPE_FRIEND = 3;

    private final NotificationMapper notificationMapper;

    /** 写入一条通知（忽略 userId 为 null，避免脏调用） */
    @Transactional
    public void notify(Long userId, int type, String content) {
        if (userId == null || content == null || content.isBlank()) {
            return;
        }
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setContent(content);
        n.setIsRead(0);
        notificationMapper.insert(n);
    }

    public PageResult<NotificationVO> page(Long userId, Boolean unreadOnly, int page, int size) {
        PageHelper.startPage(page, size);
        List<NotificationVO> list = notificationMapper.selectPage(userId, unreadOnly);
        return PageResult.of(new PageInfo<>(list));
    }

    public int unreadCount(Long userId) {
        return notificationMapper.selectUnreadCount(userId);
    }

    public void markRead(Long id, Long userId) {
        if (notificationMapper.markRead(id, userId) == 0) {
            throw new BusinessException("通知不存在");
        }
    }

    public void markAllRead(Long userId) {
        notificationMapper.markAllRead(userId);
    }
}
