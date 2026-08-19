package com.bookcycle.mapper;

import com.bookcycle.entity.OperationLog;
import com.bookcycle.vo.OperationLogVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志 Mapper：insert 用注解，分页查询走 XML。
 */
public interface OperationLogMapper {

    @Insert("INSERT INTO operation_log (operator_id, action, detail) VALUES (#{operatorId}, #{action}, #{detail})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OperationLog log);

    /** 分页查询（操作类型/操作人筛选，SQL 在 XML） */
    List<OperationLogVO> selectPage(@Param("action") String action, @Param("operatorId") Long operatorId);
}
