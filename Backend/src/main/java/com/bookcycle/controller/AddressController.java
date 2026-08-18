package com.bookcycle.controller;

import com.bookcycle.common.Result;
import com.bookcycle.dto.address.AddressRequest;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.service.AddressService;
import com.bookcycle.vo.AddressVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 收货地址接口（需登录）。
 */
@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public Result<List<AddressVO>> list() {
        return Result.ok(addressService.list(SecurityUtils.currentUserId()));
    }

    @PostMapping
    public Result<AddressVO> create(@Valid @RequestBody AddressRequest req) {
        return Result.ok(addressService.create(req, SecurityUtils.currentUserId()));
    }

    @PutMapping("/{id}")
    public Result<AddressVO> update(@PathVariable Long id, @Valid @RequestBody AddressRequest req) {
        return Result.ok(addressService.update(id, req, SecurityUtils.currentUserId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(id, SecurityUtils.currentUserId());
        return Result.ok();
    }

    /** 设为默认（同一用户仅一个默认） */
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id, SecurityUtils.currentUserId());
        return Result.ok();
    }
}
