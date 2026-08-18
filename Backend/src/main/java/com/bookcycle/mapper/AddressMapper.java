package com.bookcycle.mapper;

import com.bookcycle.entity.Address;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 收货地址 Mapper：简单 SQL 用注解（默认地址排前）。
 */
public interface AddressMapper {

    @Select("SELECT * FROM address WHERE user_id = #{userId} AND deleted = 0 ORDER BY is_default DESC, id DESC")
    List<Address> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM address WHERE id = #{id} AND deleted = 0")
    Address selectById(@Param("id") Long id);

    @Insert("INSERT INTO address (user_id, receiver_name, phone, province, city, district, detail, is_default) " +
            "VALUES (#{userId}, #{receiverName}, #{phone}, #{province}, #{city}, #{district}, #{detail}, #{isDefault})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Address address);

    @Update("UPDATE address SET receiver_name = #{receiverName}, phone = #{phone}, province = #{province}, " +
            "city = #{city}, district = #{district}, detail = #{detail} WHERE id = #{id} AND user_id = #{userId}")
    int update(Address address);

    @Update("UPDATE address SET deleted = 1 WHERE id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE address SET is_default = 0 WHERE user_id = #{userId} AND is_default = 1")
    int clearDefault(@Param("userId") Long userId);

    @Update("UPDATE address SET is_default = 1 WHERE id = #{id} AND user_id = #{userId}")
    int setDefault(@Param("id") Long id, @Param("userId") Long userId);
}
