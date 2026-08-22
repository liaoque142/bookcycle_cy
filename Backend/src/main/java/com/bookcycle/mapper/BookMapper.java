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

    /** 书籍详情（含当前用户是否已收藏 favorited；游客传 null 恒 false，SQL 在 XML） */
    BookVO selectDetailVO(@Param("id") Long id, @Param("currentUserId") Long currentUserId);

    /** 某用户收藏的书籍分页（我的收藏 / 好友收藏共用，SQL 在 XML） */
    List<BookVO> selectFavoritesByUser(@Param("userId") Long userId);

    /** 书市分页列表（过滤 + 排序，SQL 在 XML） */
    List<BookVO> selectPage(@Param("keyword") String keyword,
                            @Param("categoryId") Long categoryId,
                            @Param("tradeType") Integer tradeType,
                            @Param("condition") Integer condition,
                            @Param("sort") String sort);

    /** 书籍实体（订单服务需要数值枚举/售价/卖家，而非中文 VO） */
    @Select("SELECT * FROM book WHERE id = #{id} AND deleted = 0")
    Book selectEntityById(@Param("id") Long id);

    /** 我发布的书籍分页（交换发起选书等；status 可选过滤，SQL 在 XML） */
    List<BookVO> selectMinePage(@Param("userId") Long userId, @Param("status") Integer status);

    /** 书籍状态 CAS 原子变更（防超卖：在售→已预订 / 已预订→在售 / 已预订→已售出） */
    @Update("UPDATE book SET status = #{to} WHERE id = #{id} AND status = #{from}")
    int updateStatusIfEquals(@Param("id") Long id, @Param("from") Integer from, @Param("to") Integer to);

    /** 分类下书籍计数（删除分类前校验） */
    @Select("SELECT COUNT(*) FROM book WHERE category_id = #{categoryId} AND deleted = 0")
    int countByCategoryId(@Param("categoryId") Long categoryId);

    // ===== 管理后台 =====

    /** 管理后台书籍分页列表（全状态，关键词/状态/交易方式/分类筛选，SQL 在 XML） */
    List<BookVO> selectAdminPage(@Param("keyword") String keyword,
                                 @Param("status") Integer status,
                                 @Param("tradeType") Integer tradeType,
                                 @Param("categoryId") Long categoryId);
}
