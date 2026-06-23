package net.de5.yeoh.bingocv.service;

import net.de5.yeoh.bingocv.model.domain.Skill;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author yeoh
* @description 针对表【bingo_skill(用户技能表)】的数据库操作Service
* @createDate 2026-05-10 20:11:56
*/
public interface SkillService extends IService<Skill> {

    /**
     * 添加技能
     * @param skill 技能信息
     * @return 保存后的技能
     */
    Skill add(Skill skill);

    /**
     * 根据用户ID查询技能信息
     * @param userId 用户ID
     * @return 技能信息
     */
    Skill getByUserId(Long userId);
}
