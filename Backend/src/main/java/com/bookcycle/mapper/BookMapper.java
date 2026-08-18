package com.bookcycle.mapper;

import com.bookcycle.entity.Book;
import com.bookcycle.vo.BookVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    /** 书籍实体（订单服务需要数值枚举/售价/卖家，而非中文 VO） */
    @Select("SELECT * FROM book WHERE id = #{id} AND deleted = 0")
    Book selectEntityById(@Param("id") Long id);

    /** 书籍状态 CAS 原子变更（防超卖：在售→已预订 / 已预订→在售 / 已预订→已售出） */
    @Update("UPDATE book SET status = #{to} WHERE id = #{id} AND status = #{from}")
    int updateStatusIfEquals(@Param("id") Long id, @Param("from") Integer from, @Param("to") Integer to);
}
