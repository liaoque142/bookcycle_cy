package com.bookcycle.mapper;

import com.bookcycle.entity.UserProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    /** 更新我的资料（头像/昵称/简介/兴趣标签/书籍喜好），按 user_id 更新 */
    @Update("UPDATE user_profile SET avatar = #{avatar}, nickname = #{nickname}, bio = #{bio}, " +
            "interest_tags = #{interestTags}, genre_ids = #{genreIds}, update_time = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId} AND deleted = 0")
    int updateByUserId(UserProfile profile);
}
