package com.bookcycle.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理后台：设置用户角色入参。
 */
@Data
public class UserRoleRequest {

    /** 角色编码：admin / user */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码最长 50 字符")
    private String roleCode;
}
