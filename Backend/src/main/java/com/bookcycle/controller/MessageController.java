package com.bookcycle.controller;

import com.bookcycle.common.Result;
import com.bookcycle.dto.social.SendMessageRequest;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.service.MessageService;
import com.bookcycle.vo.MessageVO;
import com.bookcycle.vo.PeerUnreadVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 私信接口：发送 / 历史 / 已读 / 未读数（均需登录）。
 * <p>历史与未读均以当前用户为边界，第三者访问他人会话返回空列表。
 */
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** 发送私信（仅好友，黑名单已解除好友关系） */
    @PostMapping
    public Result<Void> send(@Valid @RequestBody SendMessageRequest req) {
        messageService.send(req, SecurityUtils.currentUserId());
        return Result.ok();
    }

    /** 与某好友的聊天记录（第三者访问返回空列表） */
    @GetMapping("/{userId}")
    public Result<List<MessageVO>> history(@PathVariable Long userId) {
        return Result.ok(messageService.history(userId, SecurityUtils.currentUserId()));
    }

    /** 标记该好友发来的全部消息已读 */
    @PutMapping("/{userId}/read")
    public Result<Void> read(@PathVariable Long userId) {
        messageService.markPeerRead(userId, SecurityUtils.currentUserId());
        return Result.ok();
    }

    /** 全部未读总数（Header 私信红点） */
    @GetMapping("/unread-count")
    public Result<Integer> unreadCount() {
        return Result.ok(messageService.unreadCount(SecurityUtils.currentUserId()));
    }

    /** 按好友未读数（好友行角标） */
    @GetMapping("/unread-summary")
    public Result<List<PeerUnreadVO>> unreadSummary() {
        return Result.ok(messageService.unreadSummary(SecurityUtils.currentUserId()));
    }
}
