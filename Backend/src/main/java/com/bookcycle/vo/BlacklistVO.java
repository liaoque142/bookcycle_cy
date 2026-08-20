package com.bookcycle.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 黑名单条目：userId 为被拉黑用户 ID。
 */
@Data
public class BlacklistVO {

    private Long id;
    /** 被拉黑用户 ID */
    private Long userId;
    private String username;
    private String nickname;
    /** 被拉黑用户头像（可空，前端回退首字） */
    private String avatar;
    private Integer credit;
    private LocalDateTime createTime;
}
