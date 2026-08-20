package com.bookcycle.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友：userId 为好友用户 ID，nickname 展示名（缺省回退用户名）。
 */
@Data
public class FriendVO {

    private Long id;
    /** 好友用户 ID */
    private Long userId;
    private String username;
    private String nickname;
    /** 好友头像（可空，前端回退首字） */
    private String avatar;
    private Integer credit;
    /** 成为好友时间 */
    private LocalDateTime createTime;
}
