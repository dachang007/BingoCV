package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.model.domain.Profiles;
import net.de5.yeoh.bingocv.service.ProfilesService;
import net.de5.yeoh.bingocv.mapper.ProfilesMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
* @author yeoh
* @description 针对表【bingo_profiles(个人简介及基本信息表)】的数据库操作Service实现
* @createDate 2026-05-12 14:40:47
*/
@Service
public class ProfilesServiceImpl extends ServiceImpl<ProfilesMapper, Profiles>
    implements ProfilesService {

    @Override
    public Profiles getByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<Profiles>()
                .eq(Profiles::getUserId, userId));
    }

    @Override
    public Profiles create(Profiles profile) {
        profile.setCreateTime(LocalDateTime.now());
        profile.setUpdateTime(LocalDateTime.now());
        this.save(profile);
        return profile;
    }

    @Override
    public Profiles updateProfile(Profiles profile) {
        profile.setUpdateTime(LocalDateTime.now());
        this.updateById(profile);
        return this.getById(profile.getId());
    }
}