package com.bookcycle.common;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

/**
 * 分页结果，字段对齐前端 types/api.ts 的 Page&lt;T&gt;（list / total / page / size）。
 */
@Data
public class PageResult<T> {

    private List<T> list;
    private long total;
    private int page;
    private int size;

    public static <T> PageResult<T> of(PageInfo<T> pageInfo) {
        PageResult<T> r = new PageResult<>();
        r.list = pageInfo.getList();
        r.total = pageInfo.getTotal();
        r.page = pageInfo.getPageNum();
        r.size = pageInfo.getPageSize();
        return r;
    }
}
