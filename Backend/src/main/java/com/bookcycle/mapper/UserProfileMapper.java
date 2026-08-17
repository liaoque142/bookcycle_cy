package com.bookcycle.mapper;

import com.bookcycle.entity.UserProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 * 用户详情 Mapper。
 */
public interface UserProfileMapper {

    @Select("SELECT * FROM user_profile WHERE user_id = #{userId} AND deleted = 0")
    UserProfile selectByUserId(Long userId);

    @Select("SELECT * FROM user_profile WHERE nickname = #{nickname} AND deleted = 0")
    UserProfile selectByNickname(String nickname);

    @Insert("INSERT INTO user_profile (user_id, avatar, nickname, bio, interest_tags) " +
            "VALUES (#{userId}, #{avatar}, #{nickname}, #{bio}, #{interestTags})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserProfile profile);
}
