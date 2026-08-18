package com.bookcycle.controller;

import com.bookcycle.common.PageResult;
import com.bookcycle.common.Result;
import com.bookcycle.dto.forum.CreateCommentRequest;
import com.bookcycle.dto.forum.CreatePostRequest;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.service.ForumService;
import com.bookcycle.vo.ForumBoardVO;
import com.bookcycle.vo.ForumCommentVO;
import com.bookcycle.vo.ForumLikeVO;
import com.bookcycle.vo.ForumPostVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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
 * 论坛接口：板块 / 帖子 / 评论浏览（游客可看），发帖、编辑、删除、评论、点赞（需登录）。
 */
@Validated
@RestController
@RequestMapping("/api/v1/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    @GetMapping("/boards")
    public Result<List<ForumBoardVO>> boards() {
        return Result.ok(forumService.listBoards());
    }

    /** 帖子列表：板块/关键词过滤 + latest/hot 排序 + 分页 */
    @GetMapping("/posts")
    public Result<PageResult<ForumPostVO>> posts(
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return Result.ok(forumService.pagePosts(boardId, keyword, sort, page, size));
    }

    /** 帖子详情（游客可看；登录则带 liked 已赞标记） */
    @GetMapping("/posts/{id}")
    public Result<ForumPostVO> detail(@PathVariable Long id) {
        return Result.ok(forumService.detail(id, SecurityUtils.currentUserIdOrNull()));
    }

    /** 发帖 */
    @PostMapping("/posts")
    public Result<ForumPostVO> create(@Valid @RequestBody CreatePostRequest req) {
        return Result.ok(forumService.createPost(req, SecurityUtils.currentUserId()));
    }

    /** 编辑帖子（仅本人） */
    @PutMapping("/posts/{id}")
    public Result<ForumPostVO> update(@PathVariable Long id, @Valid @RequestBody CreatePostRequest req) {
        return Result.ok(forumService.updatePost(id, req, SecurityUtils.currentUserId()));
    }

    /** 删除帖子（逻辑删除，仅本人） */
    @DeleteMapping("/posts/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        forumService.deletePost(id, SecurityUtils.currentUserId());
        return Result.ok();
    }

    /** 帖子评论列表（游客可看） */
    @GetMapping("/posts/{id}/comments")
    public Result<List<ForumCommentVO>> comments(@PathVariable Long id) {
        return Result.ok(forumService.listComments(id));
    }

    /** 发表评论 */
    @PostMapping("/comments")
    public Result<Void> comment(@Valid @RequestBody CreateCommentRequest req) {
        forumService.createComment(req, SecurityUtils.currentUserId());
        return Result.ok();
    }

    /** 点赞（幂等） */
    @PostMapping("/posts/{id}/like")
    public Result<ForumLikeVO> like(@PathVariable Long id) {
        return Result.ok(forumService.like(id, SecurityUtils.currentUserId()));
    }

    /** 取消点赞（幂等） */
    @DeleteMapping("/posts/{id}/like")
    public Result<ForumLikeVO> unlike(@PathVariable Long id) {
        return Result.ok(forumService.unlike(id, SecurityUtils.currentUserId()));
    }
}
