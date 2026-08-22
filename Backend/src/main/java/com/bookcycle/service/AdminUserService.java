package com.bookcycle.service;

import com.bookcycle.common.PageResult;
import com.bookcycle.dto.admin.AdjustCreditRequest;
import com.bookcycle.dto.admin.UserRoleRequest;
import com.bookcycle.entity.CreditLog;
import com.bookcycle.entity.Role;
import com.bookcycle.entity.SysUser;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.CreditMapper;
import com.bookcycle.mapper.RoleMapper;
import com.bookcycle.mapper.UserMapper;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.vo.AdminUserVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理后台：用户管理（列表/详情/禁用启用/调整信誉分）。
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserMapper userMapper;
    private final CreditMapper creditMapper;
    private final RoleMapper roleMapper;
    private final OperationLogService operationLogService;

    public PageResult<AdminUserVO> page(String keyword, Integer status, int page, int size) {
        PageHelper.startPage(page, size);
        List<AdminUserVO> list = userMapper.selectAdminPage(keyword, status);
        return PageResult.of(new PageInfo<>(list));
    }

    public AdminUserVO detail(Long id) {
        AdminUserVO vo = userMapper.selectAdminDetail(id);
        if (vo == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return vo;
    }

    /** 启用/禁用（不能操作管理员账号，避免把管理后台锁死） */
    @Transactional
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态不合法");
        }
        SysUser user = requireUser(id);
        if ("admin".equals(userMapper.selectRoleCodeByUserId(id))) {
            throw new BusinessException(400, "不能禁用管理员账号");
        }
        userMapper.updateStatus(id, status);
        operationLogService.record("UPDATE_USER_STATUS", "用户 " + user.getUsername() + (status == 1 ? " 启用" : " 禁用"));
    }

    /** 调整信誉分（信用日志记操作人） */
    @Transactional
    public void adjustCredit(Long id, AdjustCreditRequest req) {
        Long operatorId = SecurityUtils.currentUserId();
        if (operatorId.equals(id)) {
            throw new BusinessException(400, "不能调整自己的信誉分");
        }
        SysUser user = requireUser(id);
        creditMapper.updateCredit(id, req.getChangeValue());
        CreditLog log = new CreditLog();
        log.setUserId(id);
        log.setChangeValue(req.getChangeValue());
        log.setReason(req.getReason());
        log.setOperatorId(operatorId);
        creditMapper.insertLog(log);
        operationLogService.record("ADJUST_CREDIT", "调整用户 " + user.getUsername()
                + " 信誉分 " + (req.getChangeValue() > 0 ? "+" : "") + req.getChangeValue()
                + "，原因：" + req.getReason());
    }

    /** 设置用户角色（仅 admin / user；不能改自己的角色，避免管理后台锁死；已同角色幂等） */
    @Transactional
    public void updateRole(Long id, UserRoleRequest req) {
        Long operatorId = SecurityUtils.currentUserId();
        if (operatorId.equals(id)) {
            throw new BusinessException(400, "不能修改自己的角色");
        }
        String roleCode = req.getRoleCode() == null ? null : req.getRoleCode().trim();
        if (!"admin".equals(roleCode) && !"user".equals(roleCode)) {
            throw new BusinessException(400, "角色不合法");
        }
        Role role = roleMapper.selectByCode(roleCode);
        if (role == null) {
            throw new BusinessException(400, "角色不存在");
        }
        SysUser user = requireUser(id);
        if (roleCode.equals(userMapper.selectRoleCodeByUserId(id))) {
            return; // 已是该角色，幂等不重复记录
        }
        userMapper.updateRole(id, role.getId());
        operationLogService.record("SET_ROLE", "用户 " + user.getUsername() + " 角色设为 " + role.getRoleName());
    }

    private SysUser requireUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }
}
