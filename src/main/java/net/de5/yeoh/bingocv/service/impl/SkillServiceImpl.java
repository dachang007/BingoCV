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
        /**
         * 业务逻辑：
         * 1. 获取当前用户ID
         * 2. 删除该用户已存在的技能记录（每个用户只有一条技能记录）
         * 3. 设置创建时间和更新时间
         * 4. 保存新的技能记录
         * 
         * 设计说明：
         * - 技能表采用单条记录存储，技能关键词以逗号分隔存储在keywords字段中
         * - 每次更新时先删除旧记录再保存新记录，保证数据一致性
         * - 使用事务确保删除和保存操作的原子性
         */
        Long userId = skill.getUserId();
        // 先删除该用户旧技能
        this.remove(new LambdaQueryWrapper<Skill>().eq(Skill::getUserId, userId));
        skill.setCreateTime(LocalDateTime.now());
        skill.setUpdateTime(LocalDateTime.now());
        this.save(skill);
        return skill;
    }

    @Override
    public Skill getByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<Skill>()
                .eq(Skill::getUserId, userId));
    }
}
