package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址表。
 */
@Data
public class Address {

    private Long id;
    private Long userId;
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    /** 是否默认：1 是 / 0 否 */
    private Integer isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0 正常 / 1 删除 */
    private Integer deleted;
}
