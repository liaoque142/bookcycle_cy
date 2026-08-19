package com.bookcycle.controller;

import com.bookcycle.common.Result;
import com.bookcycle.dto.admin.CategoryRequest;
import com.bookcycle.entity.Category;
import com.bookcycle.service.AdminCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理后台：分类管理接口（仅 admin 角色）。
 */
@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @GetMapping
    public Result<List<Category>> listAll() {
        return Result.ok(adminCategoryService.listAll());
    }

    @PostMapping
    public Result<Category> create(@Valid @RequestBody CategoryRequest req) {
        return Result.ok(adminCategoryService.create(req));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        adminCategoryService.update(id, req);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminCategoryService.delete(id);
        return Result.ok();
    }
}
