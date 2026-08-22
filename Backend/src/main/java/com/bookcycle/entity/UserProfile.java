package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户详情表：头像、昵称等展示信息与 sys_user 分离。
 */
@Data
public class UserProfile {

    private Long id;
    private Long userId;
    private String avatar;
    private String nickname;
    private String bio;
    /** 兴趣标签，逗号分隔 */
    private String interestTags;
    /** 书籍喜好分类ID，逗号分隔 */
    private String genreIds;
    /** 收款二维码 URL（买家下单后展示给买家扫码转账） */
    private String payQr;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0 正常 / 1 删除 */
    private Integer deleted;
}
