package com.bookcycle.controller;

import com.bookcycle.common.Result;
import com.bookcycle.dto.social.SendFriendRequest;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.service.FriendService;
import com.bookcycle.vo.BlacklistVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 黑名单接口：拉黑 / 解除 / 列表（需登录）。拉黑后双方无法发送好友申请与交易（下单校验）。
 */
@RestController
@RequestMapping("/api/v1/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final FriendService friendService;

    @GetMapping
    public Result<List<BlacklistVO>> list() {
        return Result.ok(friendService.listBlacklist(SecurityUtils.currentUserId()));
    }

    /** 拉黑用户（复用 SendFriendRequest.toUserId 作为目标） */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody SendFriendRequest req) {
        friendService.addBlacklist(req.getToUserId(), SecurityUtils.currentUserId());
        return Result.ok();
    }

    /** 解除拉黑 */
    @DeleteMapping("/{userId}")
    public Result<Void> remove(@PathVariable Long userId) {
        friendService.removeBlacklist(userId, SecurityUtils.currentUserId());
        return Result.ok();
    }
}
