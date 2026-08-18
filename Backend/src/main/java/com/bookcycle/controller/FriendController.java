package com.bookcycle.controller;

import com.bookcycle.common.Result;
import com.bookcycle.dto.social.HandleFriendRequest;
import com.bookcycle.dto.social.SendFriendRequest;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.service.FriendService;
import com.bookcycle.vo.FriendRequestVO;
import com.bookcycle.vo.FriendVO;
import com.bookcycle.vo.UserBriefVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 好友接口：搜索书友 / 好友列表 / 申请发送与处理 / 删除好友（均需登录）。
 */
@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /** 搜索书友（用户名/昵称模糊） */
    @GetMapping("/search")
    public Result<List<UserBriefVO>> search(@RequestParam String keyword) {
        return Result.ok(friendService.search(keyword, SecurityUtils.currentUserId()));
    }

    /** 好友列表 */
    @GetMapping
    public Result<List<FriendVO>> friends() {
        return Result.ok(friendService.listFriends(SecurityUtils.currentUserId()));
    }

    /** 发送好友申请 */
    @PostMapping("/requests")
    public Result<Void> send(@Valid @RequestBody SendFriendRequest req) {
        friendService.sendRequest(req, SecurityUtils.currentUserId());
        return Result.ok();
    }

    /** 我收到的申请（含已处理历史） */
    @GetMapping("/requests")
    public Result<List<FriendRequestVO>> requests() {
        return Result.ok(friendService.listRequests(SecurityUtils.currentUserId()));
    }

    /** 处理申请：accept 同意 / reject 拒绝 */
    @PutMapping("/requests/{id}")
    public Result<Void> handle(@PathVariable Long id, @Valid @RequestBody HandleFriendRequest req) {
        friendService.handleRequest(id, req, SecurityUtils.currentUserId());
        return Result.ok();
    }

    /** 删除好友（双向解除） */
    @DeleteMapping("/{userId}")
    public Result<Void> delete(@PathVariable Long userId) {
        friendService.deleteFriend(userId, SecurityUtils.currentUserId());
        return Result.ok();
    }
}
