package com.bookcycle.mapper;

import com.bookcycle.entity.Payment;
import com.bookcycle.entity.TradeOrder;
import com.bookcycle.vo.OrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 交易订单 Mapper：简单写操作用注解，联表/分页查询走 XML（TradeMapper.xml）。
 */
public interface TradeMapper {

    @Insert("INSERT INTO trade_order (order_no, buyer_id, seller_id, book_id, amount, trade_type, ship_type, status, address_id, cancel_reason, after_sale) " +
            "VALUES (#{orderNo}, #{buyerId}, #{sellerId}, #{bookId}, #{amount}, #{tradeType}, #{shipType}, #{status}, #{addressId}, #{cancelReason}, #{afterSale})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrder(TradeOrder order);

    @Select("SELECT * FROM trade_order WHERE order_no = #{orderNo} AND deleted = 0")
    TradeOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /** 订单状态 CAS 原子变更（防重复操作） */
    @Update("UPDATE trade_order SET status = #{to} WHERE id = #{id} AND status = #{from}")
    int updateStatus(@Param("id") Long id, @Param("from") Integer from, @Param("to") Integer to);

    @Update("UPDATE trade_order SET status = 5, cancel_reason = #{reason} WHERE id = #{id}")
    int cancel(@Param("id") Long id, @Param("reason") String reason);

    OrderVO selectOrderVO(@Param("orderNo") String orderNo, @Param("currentUserId") Long currentUserId);

    List<OrderVO> selectOrderPage(@Param("userId") Long userId,
                                  @Param("role") String role,
                                  @Param("status") Integer status,
                                  @Param("currentUserId") Long currentUserId);

    /** 管理后台订单分页列表（全量，keyword 匹配订单号/书名/买卖方用户名） */
    List<OrderVO> selectAdminOrderPage(@Param("keyword") String keyword,
                                       @Param("status") Integer status,
                                       @Param("tradeType") Integer tradeType,
                                       @Param("currentUserId") Long currentUserId);

    // ---- payment ----

    @Insert("INSERT INTO payment (order_id, amount, pay_method, pay_status, pay_time) " +
            "VALUES (#{orderId}, #{amount}, #{payMethod}, #{payStatus}, #{payTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPayment(Payment payment);

    @Select("SELECT * FROM payment WHERE order_id = #{orderId} LIMIT 1")
    Payment selectPaymentByOrderId(@Param("orderId") Long orderId);

    @Update("UPDATE payment SET pay_status = 2 WHERE order_id = #{orderId} AND pay_status = 1")
    int refundPayment(@Param("orderId") Long orderId);
}
