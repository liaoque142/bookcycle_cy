package com.bookcycle.mapper;

import com.bookcycle.entity.TradeEvaluation;
import com.bookcycle.vo.EvaluationVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 交易评价 Mapper：插入/幂等判断用注解，联表列表走 XML（EvaluationMapper.xml）。
 */
public interface EvaluationMapper {

    @Insert("INSERT INTO trade_evaluation (order_id, evaluator_id, target_id, score, content) " +
            "VALUES (#{orderId}, #{evaluatorId}, #{targetId}, #{score}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TradeEvaluation evaluation);

    @Select("SELECT * FROM trade_evaluation WHERE order_id = #{orderId} AND evaluator_id = #{evaluatorId}")
    TradeEvaluation selectByOrderEvaluator(@Param("orderId") Long orderId, @Param("evaluatorId") Long evaluatorId);

    List<EvaluationVO> selectByOrderId(@Param("orderId") Long orderId);
}
