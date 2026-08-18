package com.bookcycle.controller;

import com.bookcycle.common.Result;
import com.bookcycle.dto.social.CreateDiscussionRequest;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.service.DiscussionService;
import com.bookcycle.vo.DiscussionVO;
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
 * 书籍讨论接口：列表游客可看（GET /api/v1/books/** 已放行），发表 / 删除需登录。
 */
@RestController
@RequestMapping("/api/v1/books/{bookId}/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    @GetMapping
    public Result<List<DiscussionVO>> list(@PathVariable Long bookId) {
        return Result.ok(discussionService.list(bookId));
    }

    /** 发表讨论 / 回复（parentId 为空为顶级） */
    @PostMapping
    public Result<Void> create(@PathVariable Long bookId, @Valid @RequestBody CreateDiscussionRequest req) {
        discussionService.create(bookId, req, SecurityUtils.currentUserId());
        return Result.ok();
    }

    /** 删除讨论（仅本人或书籍作者） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        discussionService.delete(id, SecurityUtils.currentUserId());
        return Result.ok();
    }
}
