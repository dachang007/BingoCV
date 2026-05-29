package net.de5.yeoh.bingocv.service;

import net.de5.yeoh.bingocv.model.domain.Profiles;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author yeoh
* @description 针对表【bingo_profiles(个人简介及基本信息表)】的数据库操作Service
* @createDate 2026-05-12 14:40:47
*/
public interface ProfilesService extends IService<Profiles> {

    /**
     * 根据用户ID获取个人信息
     * @param userId 用户ID
     * @return 个人信息
     */
    Profiles getByUserId(Long userId);

    /**
     * 创建个人信息
     * @param profile 个人信息
     * @return 创建后的个人信息
     */
    Profiles create(Profiles profile);

    /**
     * 更新个人信息
     * @param profile 个人信息
     * @return 更新后的个人信息
     */
    Profiles updateProfile(Profiles profile);
}