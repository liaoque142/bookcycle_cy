package com.bookcycle.service;

import com.bookcycle.common.PageResult;
import com.bookcycle.entity.Category;
import com.bookcycle.mapper.CategoryMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务。
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 根分类分页列表（脚手架冒烟：验证 PageHelper + XML Mapper 链路）。
     */
    public PageResult<Category> listRoot(int page, int size) {
        PageHelper.startPage(page, size);
        List<Category> list = categoryMapper.selectRootList();
        return PageResult.of(new PageInfo<>(list));
    }
}
