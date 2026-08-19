package com.bookcycle.service;

import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.StatsMapper;
import com.bookcycle.vo.DayCountVO;
import com.bookcycle.vo.StatsOverviewVO;
import com.bookcycle.vo.StatsTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台：数据统计（概览指标 + 每日趋势）。
 */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final StatsMapper statsMapper;

    public StatsOverviewVO overview() {
        StatsOverviewVO vo = new StatsOverviewVO();
        vo.setUserCount(statsMapper.countUsers());
        vo.setBookCount(statsMapper.countBooks());
        vo.setOrderCount(statsMapper.countOrders());
        vo.setTodayOrders(statsMapper.countTodayOrders());
        vo.setTodayNewUsers(statsMapper.countTodayNewUsers());
        vo.setTotalAmount(statsMapper.sumTradeAmount());
        vo.setPendingRefund(statsMapper.countPendingRefund());
        return vo;
    }

    /** 近 N 天每日订单/新增用户趋势（补零对齐日期，1-30 天） */
    public List<StatsTrendVO> trend(int days) {
        if (days < 1 || days > 30) {
            throw new BusinessException(400, "天数需在 1-30 之间");
        }
        Map<String, Long> orderMap = toMap(statsMapper.selectOrdersByDay(days));
        Map<String, Long> userMap = toMap(statsMapper.selectUsersByDay(days));
        LocalDate today = LocalDate.now();
        List<StatsTrendVO> result = new ArrayList<>(days);
        for (int i = days - 1; i >= 0; i--) {
            String date = today.minusDays(i).format(DATE_FMT);
            StatsTrendVO vo = new StatsTrendVO();
            vo.setDate(date);
            vo.setOrderCount(orderMap.getOrDefault(date, 0L));
            vo.setUserCount(userMap.getOrDefault(date, 0L));
            result.add(vo);
        }
        return result;
    }

    private Map<String, Long> toMap(List<DayCountVO> list) {
        Map<String, Long> map = new HashMap<>();
        for (DayCountVO vo : list) {
            map.put(vo.getDate(), vo.getCount());
        }
        return map;
    }
}
