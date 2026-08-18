package com.bookcycle.vo;

import lombok.Data;

/**
 * 收货地址展示 VO。
 */
@Data
public class AddressVO {

    private Long id;
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Boolean isDefault;
}
