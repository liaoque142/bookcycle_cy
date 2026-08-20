package com.bookcycle.service;

import com.bookcycle.dto.auth.LoginRequest;
import com.bookcycle.dto.auth.LoginResultVO;
import com.bookcycle.dto.auth.RegisterRequest;
import com.bookcycle.entity.Role;
import com.bookcycle.entity.SysUser;
import com.bookcycle.entity.UserProfile;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.RoleMapper;
import com.bookcycle.mapper.UserMapper;
import com.bookcycle.mapper.UserProfileMapper;
import com.bookcycle.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 认证服务：注册 / 登录。
 * <p>注册走 RBAC 统一账号（sys_user + role，默认 user 角色），BCrypt 加密。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResultVO login(LoginRequest req) {
        SysUser user = findByAccount(req.getAccount().trim());
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
        userMapper.updateLastLoginTime(user.getId(), LocalDateTime.now());
        Role role = roleMapper.selectById(user.getRoleId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role.getRoleCode());
        return LoginResultVO.builder().token(token).user(userService.toUserVO(user, role)).build();
    }

    /** 注册：校验 → 插 sys_user + user_profile，账号唯一（手机号或邮箱） */
    @Transactional
    public void register(RegisterRequest req) {
        String account = req.getAccount().trim();
        if (!isPhone(account) && !isEmail(account)) {
            throw new BusinessException("请输入正确的手机号或邮箱");
        }
        if (findByAccount(account) != null) {
            throw new BusinessException("该账号已注册，请直接登录");
        }
        String nickname = req.getNickname() == null ? null : req.getNickname().trim();
        if (StringUtils.hasText(nickname) && userProfileMapper.selectByNickname(nickname) != null) {
            throw new BusinessException("该昵称已被占用");
        }
        Role userRole = roleMapper.selectByCode("user");
        SysUser user = new SysUser();
        user.setUsername(account); // 用户名复用账号（手机号/邮箱唯一）
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        if (isPhone(account)) {
            user.setPhone(account);
        } else {
            user.setEmail(account);
        }
        user.setCredit(100);
        user.setStatus(1);
        user.setRoleId(userRole.getId());
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该账号已注册，请直接登录");
        }
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setNickname(StringUtils.hasText(nickname) ? nickname : null);
        try {
            userProfileMapper.insert(profile);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该昵称已被占用");
        }
    }

    private SysUser findByAccount(String account) {
        SysUser user = userMapper.selectByPhone(account);
        if (user == null) {
            user = userMapper.selectByEmail(account);
        }
        return user;
    }

    private boolean isPhone(String s) {
        return PHONE.matcher(s).matches();
    }

    private boolean isEmail(String s) {
        return EMAIL.matcher(s).matches();
    }
}
