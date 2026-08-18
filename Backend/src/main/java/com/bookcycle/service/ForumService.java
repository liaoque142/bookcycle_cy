package com.bookcycle.service;

import com.bookcycle.common.PageResult;
import com.bookcycle.dto.forum.CreateCommentRequest;
import com.bookcycle.dto.forum.CreatePostRequest;
import com.bookcycle.entity.ForumBoard;
import com.bookcycle.entity.ForumComment;
import com.bookcycle.entity.ForumPost;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.ForumMapper;
import com.bookcycle.vo.ForumBoardVO;
import com.bookcycle.vo.ForumCommentVO;
import com.bookcycle.vo.ForumLikeVO;
import com.bookcycle.vo.ForumPostVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 论坛服务：板块 / 帖子（发布、编辑、删除、详情）/ 评论 / 点赞。
 */
@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumMapper forumMapper;

    public List<ForumBoardVO> listBoards() {
        return forumMapper.selectBoards();
    }

    public PageResult<ForumPostVO> pagePosts(Long boardId, String keyword, String sort, int page, int size) {
        PageHelper.startPage(page, size);
        List<ForumPostVO> list = forumMapper.selectPostPage(boardId, keyword, sort);
        return PageResult.of(new PageInfo<>(list));
    }

    /** 帖子详情：浏览数 +1；currentUserId 可为 null（游客） */
    public ForumPostVO detail(Long id, Long currentUserId) {
        ForumPostVO vo = forumMapper.selectPostById(id, currentUserId);
        if (vo == null) {
            throw new BusinessException("帖子不存在");
        }
        forumMapper.incrementView(id);
        vo.setViewCount(vo.getViewCount() + 1);
        return vo;
    }

    @Transactional
    public ForumPostVO createPost(CreatePostRequest req, Long userId) {
        requireBoard(req.getBoardId());
        ForumPost post = new ForumPost();
        post.setBoardId(req.getBoardId());
        post.setUserId(userId);
        post.setTitle(req.getTitle().trim());
        post.setContent(req.getContent().trim());
        forumMapper.insertPost(post);
        return detail(post.getId(), userId);
    }

    @Transactional
    public ForumPostVO updatePost(Long id, CreatePostRequest req, Long userId) {
        requireBoard(req.getBoardId());
        checkOwnPost(id, userId);
        ForumPost update = new ForumPost();
        update.setId(id);
        update.setBoardId(req.getBoardId());
        update.setTitle(req.getTitle().trim());
        update.setContent(req.getContent().trim());
        forumMapper.updatePost(update);
        return detail(id, userId);
    }

    @Transactional
    public void deletePost(Long id, Long userId) {
        checkOwnPost(id, userId);
        forumMapper.deletePost(id);
    }

    public List<ForumCommentVO> listComments(Long postId) {
        return forumMapper.selectComments(postId);
    }

    @Transactional
    public void createComment(CreateCommentRequest req, Long userId) {
        requirePost(req.getPostId());
        ForumComment comment = new ForumComment();
        comment.setPostId(req.getPostId());
        comment.setUserId(userId);
        comment.setContent(req.getContent().trim());
        forumMapper.insertComment(comment);
    }

    /** 点赞：已赞则幂等返回；forum_like 为主，like_count 事务同步 */
    @Transactional
    public ForumLikeVO like(Long postId, Long userId) {
        requirePost(postId);
        if (forumMapper.countLike(postId, userId) > 0) {
            return likeResult(true, postId);
        }
        try {
            forumMapper.insertLike(postId, userId);
        } catch (DuplicateKeyException ignored) {
            // 并发重复点赞：幂等返回
        }
        forumMapper.increaseLikeCount(postId);
        return likeResult(true, postId);
    }

    @Transactional
    public ForumLikeVO unlike(Long postId, Long userId) {
        if (forumMapper.countLike(postId, userId) == 0) {
            return likeResult(false, postId);
        }
        forumMapper.deleteLike(postId, userId);
        forumMapper.decreaseLikeCount(postId);
        return likeResult(false, postId);
    }

    private ForumLikeVO likeResult(boolean liked, Long postId) {
        ForumLikeVO vo = new ForumLikeVO();
        vo.setLiked(liked);
        Integer count = forumMapper.selectLikeCount(postId);
        vo.setLikeCount(count == null ? 0 : count);
        return vo;
    }

    private void requireBoard(Long boardId) {
        ForumBoard board = forumMapper.selectBoardById(boardId);
        if (board == null || !Objects.equals(board.getStatus(), 1)) {
            throw new BusinessException("板块不存在或已禁用");
        }
    }

    private void requirePost(Long postId) {
        if (forumMapper.selectPostById(postId, null) == null) {
            throw new BusinessException("帖子不存在");
        }
    }

    /** 校验帖子存在且为本人所有，否则 403 */
    private void checkOwnPost(Long id, Long userId) {
        ForumPostVO post = forumMapper.selectPostById(id, userId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        if (!Objects.equals(post.getAuthorId(), userId)) {
            throw new BusinessException(403, "只能操作自己的帖子");
        }
    }
}
