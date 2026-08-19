package com.bookcycle.mapper;

import com.bookcycle.vo.DayCountVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理后台统计 Mapper：概览指标 + 每日趋势（聚合 SQL 简单，全注解）。
 */
public interface StatsMapper {

    @Select("SELECT COUNT(*) FROM sys_user WHERE deleted = 0")
    Long countUsers();

    @Select("SELECT COUNT(*) FROM book WHERE deleted = 0")
    Long countBooks();

    @Select("SELECT COUNT(*) FROM trade_order WHERE deleted = 0")
    Long countOrders();

    @Select("SELECT COUNT(*) FROM trade_order WHERE deleted = 0 AND DATE(create_time) = CURDATE()")
    Long countTodayOrders();

    /** 今日新增书友（排除 admin） */
    @Select("SELECT COUNT(*) FROM sys_user su JOIN role r ON su.role_id = r.id " +
            "WHERE r.role_code = 'user' AND su.deleted = 0 AND DATE(su.create_time) = CURDATE()")
    Long countTodayNewUsers();

    /** 交易额：待收货 + 已完成订单金额合计 */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM trade_order WHERE deleted = 0 AND status IN (3, 4)")
    BigDecimal sumTradeAmount();

    /** 待处理售后申请数 */
    @Select("SELECT COUNT(*) FROM refund WHERE status = 1")
    Long countPendingRefund();

    /** 近 N 天订单数按日聚合 */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date, COUNT(*) AS count " +
            "FROM trade_order WHERE deleted = 0 " +
            "AND create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} - 1 DAY) " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') ORDER BY date")
    List<DayCountVO> selectOrdersByDay(@Param("days") int days);

    /** 近 N 天新增用户数按日聚合 */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date, COUNT(*) AS count " +
            "FROM sys_user WHERE deleted = 0 " +
            "AND create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} - 1 DAY) " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') ORDER BY date")
    List<DayCountVO> selectUsersByDay(@Param("days") int days);
}
