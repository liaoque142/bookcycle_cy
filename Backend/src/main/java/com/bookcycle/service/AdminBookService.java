package com.bookcycle.service;

import com.bookcycle.common.PageResult;
import com.bookcycle.entity.Book;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.BookMapper;
import com.bookcycle.vo.BookVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理后台：书籍管理（全状态列表/下架/上架）。
 */
@Service
@RequiredArgsConstructor
public class AdminBookService {

    private final BookMapper bookMapper;
    private final OperationLogService operationLogService;

    public PageResult<BookVO> page(String keyword, Integer status, Integer tradeType, Long categoryId, int page, int size) {
        PageHelper.startPage(page, size);
        List<BookVO> list = bookMapper.selectAdminPage(keyword, status, tradeType, categoryId);
        return PageResult.of(new PageInfo<>(list));
    }

    /** 下架（在售1→已下架4） / 上架（已下架4→在售1）；交易中（已预订/已售出）不可操作 */
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Book book = bookMapper.selectEntityById(id);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        int rows;
        if (status == 4) {
            rows = bookMapper.updateStatusIfEquals(id, 1, 4);
        } else if (status == 1) {
            rows = bookMapper.updateStatusIfEquals(id, 4, 1);
        } else {
            throw new BusinessException(400, "仅支持下架或上架");
        }
        if (rows == 0) {
            throw new BusinessException(400, "书籍当前状态不支持该操作（交易中的书籍不可下架/上架）");
        }
        operationLogService.record("UPDATE_BOOK_STATUS", (status == 4 ? "下架" : "上架") + "书籍：" + book.getTitle());
    }
}
