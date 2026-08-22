package com.bookcycle.service;

import com.bookcycle.dto.user.UpdateProfileRequest;
import com.bookcycle.entity.Role;
import com.bookcycle.entity.SysUser;
import com.bookcycle.entity.UserProfile;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.RoleMapper;
import com.bookcycle.mapper.UserMapper;
import com.bookcycle.mapper.UserProfileMapper;
import com.bookcycle.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户资料服务：获取我的资料 / 更新我的资料（头像、昵称、简介、兴趣标签、书籍喜好）。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserProfileMapper userProfileMapper;

    /** 组装 UserVO（sys_user + user_profile + role），登录与管理接口共用 */
    public UserVO toUserVO(SysUser user, Role role) {
        UserProfile p = userProfileMapper.selectByUserId(user.getId());
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(p != null ? p.getNickname() : null)
                .avatar(p != null ? p.getAvatar() : null)
                .phone(user.getPhone())
                .email(user.getEmail())
                .credit(user.getCredit())
                .role(role.getRoleCode())
                .bio(p != null ? p.getBio() : null)
                .interestTags(p != null ? p.getInterestTags() : null)
                .genreIds(parseGenreIds(p != null ? p.getGenreIds() : null))
                .payQr(p != null ? p.getPayQr() : null)
                .build();
    }

    /** 我的资料（需登录） */
    public UserVO me(Long userId) {
        SysUser user = userMapper.selectById(userId);
        return toUserVO(user, roleMapper.selectById(user.getRoleId()));
    }

    /** 公开资料（好友详情复用）：与 me() 相同组装逻辑，含 bio/interestTags/genreIds */
    public UserVO publicProfile(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toUserVO(user, roleMapper.selectById(user.getRoleId()));
    }

    /** 更新我的资料：全字段可选，改完返回最新 UserVO 供前端同步 */
    @Transactional
    public UserVO updateProfile(Long userId, UpdateProfileRequest req) {
        UserProfile p = userProfileMapper.selectByUserId(userId);
        if (p == null) {
            p = new UserProfile();
            p.setUserId(userId);
            userProfileMapper.insert(p);
        }
        if (req.getNickname() != null) {
            String nick = req.getNickname().trim();
            UserProfile dup = userProfileMapper.selectByNickname(nick);
            if (dup != null && !dup.getUserId().equals(userId)) {
                throw new BusinessException("该昵称已被占用");
            }
            p.setNickname(StringUtils.hasText(nick) ? nick : null);
        }
        if (req.getAvatar() != null) {
            p.setAvatar(trimToNull(req.getAvatar()));
        }
        if (req.getBio() != null) {
            p.setBio(trimToNull(req.getBio()));
        }
        if (req.getInterestTags() != null) {
            p.setInterestTags(trimToNull(req.getInterestTags()));
        }
        if (req.getGenreIds() != null) {
            p.setGenreIds(joinGenreIds(req.getGenreIds()));
        }
        if (req.getPayQr() != null) {
            p.setPayQr(trimToNull(req.getPayQr()));
        }
        try {
            userProfileMapper.updateByUserId(p);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该昵称已被占用");
        }
        return me(userId);
    }

    /** "1,3" -> [1, 3]；NULL/空 -> 空列表 */
    private List<Long> parseGenreIds(String genreIds) {
        if (!StringUtils.hasText(genreIds)) {
            return Collections.emptyList();
        }
        return Arrays.stream(genreIds.split(","))
                .filter(StringUtils::hasText)
                .map(s -> Long.parseLong(s.trim()))
                .collect(Collectors.toList());
    }

    /** [1, 3] -> "1,3"；空列表 -> NULL */
    private String joinGenreIds(List<Long> genreIds) {
        if (genreIds.isEmpty()) {
            return null;
        }
        return genreIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private String trimToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }
}
