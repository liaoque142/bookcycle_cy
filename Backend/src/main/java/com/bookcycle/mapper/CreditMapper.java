package com.bookcycle.mapper;

import com.bookcycle.entity.CreditLog;
import com.bookcycle.entity.CreditRule;
import com.bookcycle.vo.CreditLogVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 信誉 Mapper：规则查询 + 分值变动（0-100 钳制）+ 变动日志。
 */
public interface CreditMapper {

    @Select("SELECT * FROM credit_rule WHERE rule_code = #{ruleCode}")
    CreditRule selectRuleByCode(@Param("ruleCode") String ruleCode);

    @Select("SELECT * FROM credit_rule WHERE id = #{id}")
    CreditRule selectRuleById(@Param("id") Long id);

    @Select("SELECT * FROM credit_rule ORDER BY priority ASC")
    List<CreditRule> selectAllRules();

    /** 规则启用/禁用（数据驱动，禁用后 CreditService.apply 自动忽略） */
    @Update("UPDATE credit_rule SET status = #{status} WHERE id = #{id}")
    int updateRuleStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 信誉分变动：仅钳下限 0（奖励可高于初始 100，扣除不转负） */
    @Update("UPDATE sys_user SET credit = GREATEST(credit + #{delta}, 0) WHERE id = #{userId}")
    int updateCredit(@Param("userId") Long userId, @Param("delta") Integer delta);

    @Select("SELECT credit FROM sys_user WHERE id = #{userId}")
    Integer selectCredit(@Param("userId") Long userId);

    @Insert("INSERT INTO credit_log (user_id, change_value, reason, operator_id) VALUES (#{userId}, #{changeValue}, #{reason}, #{operatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLog(CreditLog log);

    List<CreditLogVO> selectLogs(@Param("userId") Long userId);
}
