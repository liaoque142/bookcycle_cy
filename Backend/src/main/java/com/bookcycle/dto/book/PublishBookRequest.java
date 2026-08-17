package com.bookcycle.dto.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 发布书籍请求（枚举用编码：tradeType 1出售/2赠送/3交换，condition 1-4）。
 */
@Data
public class PublishBookRequest {

    @NotBlank(message = "请输入书名")
    @Size(max = 100, message = "书名最多 100 个字符")
    private String title;

    @NotBlank(message = "请输入作者")
    @Size(max = 50, message = "作者最多 50 个字符")
    private String author;

    @Size(max = 20, message = "ISBN 最多 20 个字符")
    private String isbn;

    @Size(max = 50, message = "出版社最多 50 个字符")
    private String publisher;

    @NotNull(message = "请选择分类")
    private Long categoryId;

    private BigDecimal originalPrice;

    /** 售价；出售必填，赠送/交换为 NULL */
    private BigDecimal price;

    @NotNull(message = "请选择交易方式")
    @Min(value = 1, message = "交易方式不正确")
    @Max(value = 3, message = "交易方式不正确")
    private Integer tradeType;

    @NotNull(message = "请选择新旧程度")
    @Min(value = 1, message = "新旧程度不正确")
    @Max(value = 4, message = "新旧程度不正确")
    private Integer condition;

    @Size(max = 500, message = "描述最多 500 个字符")
    private String description;
}
