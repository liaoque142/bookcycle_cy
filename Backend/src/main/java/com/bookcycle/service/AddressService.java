package com.bookcycle.service;

import com.bookcycle.dto.address.AddressRequest;
import com.bookcycle.entity.Address;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.AddressMapper;
import com.bookcycle.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收货地址服务：增删改查 + 设默认（同一用户仅一个默认）。
 */
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressMapper addressMapper;

    public List<AddressVO> list(Long userId) {
        return addressMapper.selectByUserId(userId).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional
    public AddressVO create(AddressRequest req, Long userId) {
        List<Address> existing = addressMapper.selectByUserId(userId);
        // 首条地址自动设为默认
        boolean asDefault = Boolean.TRUE.equals(req.getIsDefault()) || existing.isEmpty();
        if (asDefault) {
            addressMapper.clearDefault(userId);
        }
        Address address = new Address();
        address.setUserId(userId);
        fill(address, req);
        address.setIsDefault(asDefault ? 1 : 0);
        addressMapper.insert(address);
        return toVO(addressMapper.selectById(address.getId()));
    }

    @Transactional
    public AddressVO update(Long id, AddressRequest req, Long userId) {
        Address current = requireOwned(id, userId);
        boolean setDefault = Boolean.TRUE.equals(req.getIsDefault()) && current.getIsDefault() != 1;
        if (setDefault) {
            addressMapper.clearDefault(userId);
        }
        Address address = new Address();
        address.setId(id);
        address.setUserId(userId);
        fill(address, req);
        if (addressMapper.update(address) == 0) {
            throw new BusinessException("地址不存在或无权操作");
        }
        if (setDefault) {
            addressMapper.setDefault(id, userId);
        }
        return toVO(addressMapper.selectById(id));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        requireOwned(id, userId);
        addressMapper.delete(id, userId);
    }

    @Transactional
    public void setDefault(Long id, Long userId) {
        requireOwned(id, userId);
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(id, userId);
    }

    private Address requireOwned(Long id, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在或无权操作");
        }
        return address;
    }

    private void fill(Address address, AddressRequest req) {
        address.setReceiverName(req.getReceiverName().trim());
        address.setPhone(req.getPhone().trim());
        address.setProvince(req.getProvince().trim());
        address.setCity(req.getCity().trim());
        address.setDistrict(req.getDistrict().trim());
        address.setDetail(req.getDetail().trim());
    }

    private AddressVO toVO(Address a) {
        if (a == null) {
            return null;
        }
        AddressVO vo = new AddressVO();
        vo.setId(a.getId());
        vo.setReceiverName(a.getReceiverName());
        vo.setPhone(a.getPhone());
        vo.setProvince(a.getProvince());
        vo.setCity(a.getCity());
        vo.setDistrict(a.getDistrict());
        vo.setDetail(a.getDetail());
        vo.setIsDefault(a.getIsDefault() != null && a.getIsDefault() == 1);
        return vo;
    }
}
