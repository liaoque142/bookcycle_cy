package com.bookcycle.mapper;

import com.bookcycle.entity.Swap;
import com.bookcycle.vo.SwapVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 交换单 Mapper：简单读写用注解，联表 VO/分页走 XML（resources/mapper/SwapMapper.xml）。
 */
public interface SwapMapper {

    @Insert("INSERT INTO `swap` (swap_no, initiator_id, receiver_id, book_id, offer_book_id, message, status, " +
            "initiator_ship_type, initiator_address_id) " +
            "VALUES (#{swapNo}, #{initiatorId}, #{receiverId}, #{bookId}, #{offerBookId}, #{message}, #{status}, " +
            "#{initiatorShipType}, #{initiatorAddressId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Swap swap);

    @Select("SELECT * FROM `swap` WHERE id = #{id} AND deleted = 0")
    Swap selectEntityById(@Param("id") Long id);

    @Select("SELECT * FROM `swap` WHERE swap_no = #{swapNo} AND deleted = 0")
    Swap selectBySwapNo(@Param("swapNo") String swapNo);

    /** 状态 CAS（防并发重复操作）：返回 0 说明状态不匹配 */
    @Update("UPDATE `swap` SET status = #{to} WHERE id = #{id} AND status = #{from}")
    int updateStatus(@Param("id") Long id, @Param("from") Integer from, @Param("to") Integer to);

    /** 接收方同意：待对方同意 → 已同意待发货，同时记录接收方物流与收货地址（收书 B） */
    @Update("UPDATE `swap` SET status = 2, receiver_ship_type = #{shipType}, receiver_address_id = #{addressId} " +
            "WHERE id = #{id} AND status = 1")
    int accept(@Param("id") Long id, @Param("shipType") Integer shipType, @Param("addressId") Long addressId);

    /** 取消：待对方同意/已同意待发货可取消，写取消原因 */
    @Update("UPDATE `swap` SET status = 5, cancel_reason = #{reason} WHERE id = #{id} AND status IN (1, 2)")
    int cancel(@Param("id") Long id, @Param("reason") String reason);

    @Update("UPDATE `swap` SET initiator_shipped_at = NOW() WHERE id = #{id} AND initiator_shipped_at IS NULL")
    int markInitiatorShipped(@Param("id") Long id);

    @Update("UPDATE `swap` SET receiver_shipped_at = NOW() WHERE id = #{id} AND receiver_shipped_at IS NULL")
    int markReceiverShipped(@Param("id") Long id);

    /** 双方都发货后：已同意待发货 → 交换中（幂等，仅当双方 shipped_at 均已记录时生效） */
    @Update("UPDATE `swap` SET status = 3 WHERE id = #{id} AND status = 2 " +
            "AND initiator_shipped_at IS NOT NULL AND receiver_shipped_at IS NOT NULL")
    int markBothShipped(@Param("id") Long id);

    @Update("UPDATE `swap` SET initiator_confirmed_at = NOW() WHERE id = #{id} AND initiator_confirmed_at IS NULL")
    int markInitiatorConfirmed(@Param("id") Long id);

    @Update("UPDATE `swap` SET receiver_confirmed_at = NOW() WHERE id = #{id} AND receiver_confirmed_at IS NULL")
    int markReceiverConfirmed(@Param("id") Long id);

    /** 双方都确认收货后：交换中 → 已完成（幂等，仅当双方 confirmed_at 均已记录时生效） */
    @Update("UPDATE `swap` SET status = 4 WHERE id = #{id} AND status IN (2, 3) " +
            "AND initiator_confirmed_at IS NOT NULL AND receiver_confirmed_at IS NOT NULL")
    int markBothConfirmed(@Param("id") Long id);

    /** 我的交换分页：role=initiator|receiver + 状态筛选（SQL 在 XML） */
    List<SwapVO> selectPage(@Param("role") String role, @Param("status") Integer status, @Param("userId") Long userId);

    /** 交换单详情（SQL 在 XML） */
    SwapVO selectVOBySwapNo(@Param("swapNo") String swapNo);
}
