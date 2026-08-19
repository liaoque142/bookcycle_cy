package com.bookcycle.service;

import com.bookcycle.common.PageResult;
import com.bookcycle.dto.book.PublishBookRequest;
import com.bookcycle.entity.Book;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.BookMapper;
import com.bookcycle.vo.BookVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 书籍服务：发布 / 列表 / 详情。
 */
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;

    @Transactional
    public BookVO publish(PublishBookRequest req, Long userId) {
        if (req.getTradeType() != null && req.getTradeType() == 1 && req.getPrice() == null) {
            throw new BusinessException("出售书籍必须填写售价");
        }
        Book book = new Book();
        book.setTitle(req.getTitle().trim());
        book.setAuthor(req.getAuthor().trim());
        book.setIsbn(trimToNull(req.getIsbn()));
        book.setPublisher(trimToNull(req.getPublisher()));
        book.setCategoryId(req.getCategoryId());
        book.setUserId(userId);
        book.setOriginalPrice(req.getOriginalPrice());
        book.setPrice(req.getPrice());
        book.setTradeType(req.getTradeType());
        book.setCondition(req.getCondition());
        book.setDescription(trimToNull(req.getDescription()));
        book.setStatus(1);
        bookMapper.insert(book);
        return detail(book.getId());
    }

    public PageResult<BookVO> list(String keyword, Long categoryId, Integer tradeType, Integer condition,
                                   String sort, int page, int size) {
        PageHelper.startPage(page, size);
        List<BookVO> list = bookMapper.selectPage(keyword, categoryId, tradeType, condition, sort);
        return PageResult.of(new PageInfo<>(list));
    }

    /** 首页精选：高信誉卖家在售书 top N（复用 list 排序，游客可看） */
    public List<BookVO> featured(int size) {
        return list(null, null, null, null, "credit", 1, size).getList();
    }

    /** 我发布的书籍分页（status 可选过滤，如交换发起选在售书） */
    public PageResult<BookVO> mine(Integer status, int page, int size, Long userId) {
        PageHelper.startPage(page, size);
        List<BookVO> list = bookMapper.selectMinePage(userId, status);
        return PageResult.of(new PageInfo<>(list));
    }

    /** 首页最新上架：最新在售书 top N */
    public List<BookVO> latest(int size) {
        return list(null, null, null, null, "latest", 1, size).getList();
    }

    public BookVO detail(Long id) {
        BookVO vo = bookMapper.selectById(id);
        if (vo == null) {
            throw new BusinessException("书籍不存在");
        }
        return vo;
    }

    private static String trimToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }
}
