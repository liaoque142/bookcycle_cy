package com.bookcycle.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新个人资料请求：全部字段可选，未传（null）不修改。
 */
@Data
public class UpdateProfileRequest {

    @Size(max = 255, message = "头像地址过长")
    private String avatar;

    @Size(max = 50, message = "昵称最多 50 个字符")
    private String nickname;

    @Size(max = 255, message = "个人简介最多 255 个字符")
    private String bio;

    @Size(max = 255, message = "兴趣标签过长")
    private String interestTags;

    /** 书籍喜好分类ID（多选，逗号分隔存储） */
    private List<Long> genreIds;

    /** 收款二维码 URL */
    @Size(max = 255, message = "收款码地址过长")
    private String payQr;
}
