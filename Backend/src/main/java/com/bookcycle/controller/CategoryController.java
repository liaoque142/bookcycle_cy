package com.bookcycle.controller;

import com.bookcycle.common.PageResult;
import com.bookcycle.common.Result;
import com.bookcycle.entity.Category;
import com.bookcycle.service.CategoryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分类接口。当前为脚手架冒烟验证，完整分类管理模块属阶段二。
 */
@Validated
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<PageResult<Category>> listRoot(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return Result.ok(categoryService.listRoot(page, size));
    }
}
