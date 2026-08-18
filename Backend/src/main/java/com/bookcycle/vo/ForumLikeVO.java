package com.bookcycle.vo;

import lombok.Data;

/**
 * 点赞结果：是否已点赞 + 最新点赞数。
 */
@Data
public class ForumLikeVO {

    private Boolean liked;
    private Integer likeCount;
}
