package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.model.domain.Skill;
import net.de5.yeoh.bingocv.service.SkillService;
import net.de5.yeoh.bingocv.mapper.SkillMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
* @author yeoh
* @description 针对表【bingo_skill(用户技能表)】的数据库操作Service实现
* @createDate 2026-05-10 20:11:56
*/
@Service
public class SkillServiceImpl extends ServiceImpl<SkillMapper, Skill>
    implements SkillService{

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Skill add(Skill skill) {
        Long userId = skill.getUserId();
        // 先删除该用户旧技能
        this.remove(new LambdaQueryWrapper<Skill>().eq(Skill::getUserId, userId));
        skill.setCreateTime(LocalDateTime.now());
        skill.setUpdateTime(LocalDateTime.now());
        this.save(skill);
        return skill;
    }
}
