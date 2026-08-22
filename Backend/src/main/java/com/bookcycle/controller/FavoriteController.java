package com.bookcycle.controller;

import com.bookcycle.common.PageResult;
import com.bookcycle.common.Result;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.service.FavoriteService;
import com.bookcycle.vo.BookVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收藏接口：收藏/取消书籍、我的收藏分页（均需登录）。
 * <p>好友收藏走 /api/v1/friends/{userId}/favorites（好友门控，见 FriendController）。
 */
@Validated
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /** 收藏书籍 */
    @PostMapping("/{bookId}")
    public Result<Void> add(@PathVariable Long bookId) {
        favoriteService.add(bookId, SecurityUtils.currentUserId());
        return Result.ok();
    }

    /** 取消收藏 */
    @DeleteMapping("/{bookId}")
    public Result<Void> remove(@PathVariable Long bookId) {
        favoriteService.remove(bookId, SecurityUtils.currentUserId());
        return Result.ok();
    }

    /** 我的收藏分页 */
    @GetMapping("/me")
    public Result<PageResult<BookVO>> me(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        return Result.ok(favoriteService.myFavorites(SecurityUtils.currentUserId(), page, size));
    }
}
