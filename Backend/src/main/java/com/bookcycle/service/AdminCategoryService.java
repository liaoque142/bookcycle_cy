package com.bookcycle.service;

import com.bookcycle.dto.admin.CategoryRequest;
import com.bookcycle.entity.Category;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.BookMapper;
import com.bookcycle.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理后台：分类管理（增删改查，删除前校验子分类/书籍占用）。
 */
@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final CategoryMapper categoryMapper;
    private final BookMapper bookMapper;
    private final OperationLogService operationLogService;

    public List<Category> listAll() {
        return categoryMapper.selectAll();
    }

    @Transactional
    public Category create(CategoryRequest req) {
        Long parentId = resolveParentId(req.getParentId());
        checkParent(parentId);
        checkDuplicateName(req.getName(), parentId, null);
        Category c = new Category();
        c.setParentId(parentId);
        c.setName(req.getName());
        c.setSort(req.getSort() == null ? 0 : req.getSort());
        c.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        categoryMapper.insert(c);
        operationLogService.record("CREATE_CATEGORY", "新增分类：" + c.getName());
        return c;
    }

    @Transactional
    public void update(Long id, CategoryRequest req) {
        Category exist = categoryMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "分类不存在");
        }
        Long parentId = resolveParentId(req.getParentId());
        if (parentId.equals(id)) {
            throw new BusinessException(400, "父分类不能是自己");
        }
        checkParent(parentId);
        checkDuplicateName(req.getName(), parentId, id);
        exist.setParentId(parentId);
        exist.setName(req.getName());
        exist.setSort(req.getSort() == null ? exist.getSort() : req.getSort());
        exist.setStatus(req.getStatus() == null ? exist.getStatus() : req.getStatus());
        categoryMapper.update(exist);
        operationLogService.record("UPDATE_CATEGORY", "修改分类：" + exist.getName());
    }

    /** 逻辑删除（有子分类或分类下仍有书籍时拒绝） */
    @Transactional
    public void delete(Long id) {
        Category exist = categoryMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (categoryMapper.countChildren(id) > 0) {
            throw new BusinessException(400, "该分类下存在子分类，无法删除");
        }
        if (bookMapper.countByCategoryId(id) > 0) {
            throw new BusinessException(400, "该分类下仍有书籍，无法删除");
        }
        categoryMapper.deleteById(id);
        operationLogService.record("DELETE_CATEGORY", "删除分类：" + exist.getName());
    }

    private Long resolveParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    private void checkParent(Long parentId) {
        if (parentId != 0 && categoryMapper.selectById(parentId) == null) {
            throw new BusinessException(400, "父分类不存在");
        }
    }

    private void checkDuplicateName(String name, Long parentId, Long excludeId) {
        if (categoryMapper.countByNameAndParentExclude(name, parentId, excludeId) > 0) {
            throw new BusinessException(400, "同级已存在同名分类");
        }
    }
}
