package com.bookcycle.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 收货地址新增/编辑入参。
 */
@Data
public class AddressRequest {

    @NotBlank(message = "收货人不能为空")
    @Size(max = 50)
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    @Size(max = 20)
    private String phone;

    @NotBlank(message = "省份不能为空")
    @Size(max = 50)
    private String province;

    @NotBlank(message = "城市不能为空")
    @Size(max = 50)
    private String city;

    @NotBlank(message = "区县不能为空")
    @Size(max = 50)
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 200)
    private String detail;

    /** 是否设为默认（可空，默认否；首条地址自动默认） */
    private Boolean isDefault;
}
