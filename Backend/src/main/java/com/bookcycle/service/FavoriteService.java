package com.bookcycle.service;

import com.bookcycle.common.PageResult;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.BookMapper;
import com.bookcycle.mapper.FavoriteMapper;
import com.bookcycle.vo.BookVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收藏服务：收藏 / 取消 / 我的收藏分页（好友收藏走 FriendService 门控后复用 listByUser）。
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final BookMapper bookMapper;

    /** 收藏（INSERT IGNORE，重复收藏幂等不报错） */
    public void add(Long bookId, Long userId) {
        if (bookMapper.selectEntityById(bookId) == null) {
            throw new BusinessException("书籍不存在");
        }
        favoriteMapper.insert(userId, bookId);
    }

    /** 取消收藏（不存在也幂等） */
    public void remove(Long bookId, Long userId) {
        favoriteMapper.deleteByUserBook(userId, bookId);
    }

    /** 我的收藏分页 */
    public PageResult<BookVO> myFavorites(Long userId, int page, int size) {
        return listByUser(userId, page, size);
    }

    /** 某用户收藏分页（好友收藏复用；好友关系校验在 FriendService） */
    public PageResult<BookVO> listByUser(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        List<BookVO> list = bookMapper.selectFavoritesByUser(userId);
        return PageResult.of(new PageInfo<>(list));
    }
}
