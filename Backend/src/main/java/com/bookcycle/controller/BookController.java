package com.bookcycle.controller;

import com.bookcycle.common.PageResult;
import com.bookcycle.common.Result;
import com.bookcycle.dto.book.PublishBookRequest;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.service.BookService;
import com.bookcycle.vo.BookVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 书籍接口：发布（需登录）/ 列表 / 详情（游客可浏览）。
 */
@Validated
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /** 发布书籍：卖家 = 当前登录用户 */
    @PostMapping
    public Result<BookVO> publish(@Valid @RequestBody PublishBookRequest req) {
        return Result.ok(bookService.publish(req, SecurityUtils.currentUserId()));
    }

    /** 书市列表：关键词/分类/交易方式/新旧程度过滤 + 排序 + 分页 */
    @GetMapping
    public Result<PageResult<BookVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(required = false) Integer condition,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return Result.ok(bookService.list(keyword, categoryId, tradeType, condition, sort, page, size));
    }

    @GetMapping("/{id}")
    public Result<BookVO> detail(@PathVariable Long id) {
        return Result.ok(bookService.detail(id));
    }
}
