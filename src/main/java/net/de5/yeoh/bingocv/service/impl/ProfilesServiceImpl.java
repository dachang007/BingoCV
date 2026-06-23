package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.mapper.ProfilesMapper;
import net.de5.yeoh.bingocv.model.domain.Profiles;
import net.de5.yeoh.bingocv.service.ProfilesService;
import net.de5.yeoh.bingocv.service.TaskCompletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProfilesServiceImpl extends ServiceImpl<ProfilesMapper, Profiles>
    implements ProfilesService {

    @Autowired
    private TaskCompletionService taskCompletionService;

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
        
        // 检查是否完成了完善资料任务
        if (isProfileCompleted(profile)) {
            taskCompletionService.completeTask(profile.getUserId(), "complete_profile");
        }
        
        return this.getById(profile.getId());
    }

    private boolean isProfileCompleted(Profiles profile) {
        return profile.getName() != null && !profile.getName().isEmpty()
                && profile.getEmail() != null && !profile.getEmail().isEmpty()
                && profile.getPhone() != null && !profile.getPhone().isEmpty()
                && profile.getCity() != null && !profile.getCity().isEmpty()
                && profile.getDescription() != null && !profile.getDescription().isEmpty();
    }
}
