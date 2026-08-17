package com.bookcycle.mapper;

import com.bookcycle.entity.Book;
import com.bookcycle.vo.BookVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 书籍 Mapper：简单 insert 用注解，联表/分页查询走 XML（resources/mapper/BookMapper.xml）。
 */
public interface BookMapper {

    @Insert("INSERT INTO book (title, author, isbn, publisher, category_id, user_id, original_price, price, trade_type, `condition`, description, status) " +
            "VALUES (#{title}, #{author}, #{isbn}, #{publisher}, #{categoryId}, #{userId}, #{originalPrice}, #{price}, #{tradeType}, #{condition}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Book book);

    BookVO selectById(@Param("id") Long id);

    /** 书市分页列表（过滤 + 排序，SQL 在 XML） */
    List<BookVO> selectPage(@Param("keyword") String keyword,
                            @Param("categoryId") Long categoryId,
                            @Param("tradeType") Integer tradeType,
                            @Param("condition") Integer condition,
                            @Param("sort") String sort);
}
