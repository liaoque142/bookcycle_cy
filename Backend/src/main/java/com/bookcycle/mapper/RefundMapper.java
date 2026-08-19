package com.bookcycle.mapper;

import com.bookcycle.entity.Refund;
import com.bookcycle.vo.RefundVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 退款/售后 Mapper：简单写操作用注解，联表/分页查询走 XML（RefundMapper.xml）。
 */
public interface RefundMapper {

    @Insert("INSERT INTO refund (order_id, applicant_id, type, reason, amount, status) " +
            "VALUES (#{orderId}, #{applicantId}, #{type}, #{reason}, #{amount}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Refund refund);

    @Select("SELECT * FROM refund WHERE id = #{id}")
    Refund selectById(@Param("id") Long id);

    /** 订单未处理的售后申请（幂等校验） */
    @Select("SELECT * FROM refund WHERE order_id = #{orderId} AND status = 1 LIMIT 1")
    Refund selectPendingByOrderId(@Param("orderId") Long orderId);

    /** 处理售后：待处理 → 同意(2)/拒绝(3)/已完成(4)，CAS 防重复处理 */
    @Update("UPDATE refund SET status = #{status}, handler_id = #{handlerId}, handle_result = #{result} " +
            "WHERE id = #{id} AND status = 1")
    int handle(@Param("id") Long id, @Param("status") Integer status,
               @Param("handlerId") Long handlerId, @Param("result") String result);

    /** 管理后台售后分页列表（联表订单/书籍/申请人/处理人，SQL 在 XML） */
    List<RefundVO> selectPage(@Param("keyword") String keyword,
                              @Param("status") Integer status,
                              @Param("type") Integer type);

    /** 售后详情 VO（联表，按 id 取） */
    RefundVO selectVOById(@Param("id") Long id);
}
