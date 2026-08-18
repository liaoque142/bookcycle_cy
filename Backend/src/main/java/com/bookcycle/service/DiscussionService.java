package com.bookcycle.service;

import com.bookcycle.dto.social.CreateDiscussionRequest;
import com.bookcycle.entity.Book;
import com.bookcycle.entity.BookDiscussion;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.BookMapper;
import com.bookcycle.mapper.DiscussionMapper;
import com.bookcycle.vo.DiscussionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 书籍讨论服务：书籍详情页留言 / 回复（parent_id 组装成树），删除限本人或书籍作者。
 */
@Service
@RequiredArgsConstructor
public class DiscussionService {

    private final DiscussionMapper discussionMapper;
    private final BookMapper bookMapper;

    public List<DiscussionVO> list(Long bookId) {
        List<DiscussionVO> tops = discussionMapper.selectTop(bookId);
        List<DiscussionVO> replies = discussionMapper.selectReplies(bookId);
        Map<Long, List<DiscussionVO>> byParent = replies.stream()
                .collect(Collectors.groupingBy(DiscussionVO::getParentId));
        for (DiscussionVO top : tops) {
            top.setReplies(byParent.getOrDefault(top.getId(), List.of()));
        }
        return tops;
    }

    /** 发表讨论 / 回复：校验书籍存在、父评论归属本书 */
    @Transactional
    public void create(Long bookId, CreateDiscussionRequest req, Long userId) {
        if (bookMapper.selectEntityById(bookId) == null) {
            throw new BusinessException("书籍不存在");
        }
        if (req.getParentId() != null) {
            BookDiscussion parent = discussionMapper.selectById(req.getParentId());
            if (parent == null || !parent.getBookId().equals(bookId)) {
                throw new BusinessException("回复的评论不存在");
            }
        }
        BookDiscussion d = new BookDiscussion();
        d.setBookId(bookId);
        d.setUserId(userId);
        d.setContent(req.getContent().trim());
        d.setParentId(req.getParentId());
        discussionMapper.insert(d);
    }

    /** 删除：仅评论本人或书籍作者，逻辑删除 */
    @Transactional
    public void delete(Long id, Long userId) {
        BookDiscussion d = discussionMapper.selectById(id);
        if (d == null) {
            throw new BusinessException("评论不存在");
        }
        Book book = bookMapper.selectEntityById(d.getBookId());
        boolean isBookAuthor = book != null && book.getUserId().equals(userId);
        if (!d.getUserId().equals(userId) && !isBookAuthor) {
            throw new BusinessException(403, "只能删除自己的评论");
        }
        discussionMapper.delete(id);
    }
}
