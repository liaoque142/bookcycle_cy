package com.bookcycle.controller;

import com.bookcycle.common.PageResult;
import com.bookcycle.common.Result;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.service.NotificationService;
import com.bookcycle.vo.NotificationVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知接口：列表（未读筛选）/ 未读数 / 标记已读 / 全部已读（需登录）。
 */
@Validated
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<PageResult<NotificationVO>> list(
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return Result.ok(notificationService.page(SecurityUtils.currentUserId(), unreadOnly, page, size));
    }

    /** 未读数（Header 铃铛红点） */
    @GetMapping("/unread-count")
    public Result<Integer> unreadCount() {
        return Result.ok(notificationService.unreadCount(SecurityUtils.currentUserId()));
    }

    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        notificationService.markRead(id, SecurityUtils.currentUserId());
        return Result.ok();
    }

    @PutMapping("/read-all")
    public Result<Void> readAll() {
        notificationService.markAllRead(SecurityUtils.currentUserId());
        return Result.ok();
    }
}
