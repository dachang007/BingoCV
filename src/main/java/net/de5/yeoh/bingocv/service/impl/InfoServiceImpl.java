package net.de5.yeoh.bingocv.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.model.domain.*;
import net.de5.yeoh.bingocv.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 个人简历Service实现
 * @author: daChang
 * @date: 2026/5/11 20:56
 */
@Service
@Slf4j
public class InfoServiceImpl implements InfoService {

    @Autowired
    private EduService eduService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private SpecialtyService specialtyService;
    @Autowired
    private WorkService workService;
    @Autowired
    private ProfilesService profilesService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(Info info, Long userId) {
        // 1. 保存或更新用户个人信息
        Profiles profiles = info.getProfiles();
        if (profiles == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "个人信息不能为空");
        }

        // 使用当前登录用户ID，而不是从前端获取
        if (userId == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "用户ID不能为空");
        }
        
        // 设置userId到profiles中
        profiles.setUserId(userId);

        // 查询现有记录，存在则更新，不存在则创建
        Profiles existingProfile = profilesService.getByUserId(userId);
        if (existingProfile != null) {
            // 更新现有记录
            profiles.setId(existingProfile.getId());
            profiles.setUpdateTime(LocalDateTime.now());
            profilesService.updateById(profiles);
        } else {
            // 创建新记录
            profiles.setCreateTime(LocalDateTime.now());
            profiles.setUpdateTime(LocalDateTime.now());
            profilesService.save(profiles);
        }

        // 删除旧的子表记录（后续重新插入）
        eduService.remove(new LambdaQueryWrapper<Edu>().eq(Edu::getUserId, userId));
        workService.remove(new LambdaQueryWrapper<Work>().eq(Work::getUserId, userId));
        skillService.remove(new LambdaQueryWrapper<Skill>().eq(Skill::getUserId, userId));
        specialtyService.remove(new LambdaQueryWrapper<Specialty>().eq(Specialty::getUserId, userId));

        // 2. 保存教育经历
        List<Edu> eduList = info.getEduList();
        if (CollUtil.isNotEmpty(eduList)) {
            for (int i = 0; i < eduList.size(); i++) {
                Edu edu = eduList.get(i);
                edu.setUserId(userId);
                edu.setPriority(eduList.size() - i);
                edu.setCreateTime(LocalDateTime.now());
                edu.setUpdateTime(LocalDateTime.now());
            }
            eduService.saveBatch(eduList);
        }

        // 3. 保存工作经历
        List<Work> workList = info.getWorkList();
        if (CollUtil.isNotEmpty(workList)) {
            for (int i = 0; i < workList.size(); i++) {
                Work work = workList.get(i);
                work.setUserId(userId);
                work.setPriority(workList.size() - i);
                work.setCreateTime(LocalDateTime.now());
                work.setUpdateTime(LocalDateTime.now());
            }
            workService.saveBatch(workList);
        }

        // 4. 保存技能
        Skill skill = info.getSkill();
        if (skill != null && skill.getKeywords() != null) {
            skill.setUserId(userId);
            skill.setCreateTime(LocalDateTime.now());
            skill.setUpdateTime(LocalDateTime.now());
            skillService.save(skill);
        }

        // 5. 保存特长
        List<Specialty> specialtyList = info.getSpecialtyList();
        if (CollUtil.isNotEmpty(specialtyList)) {
            for (Specialty specialty : specialtyList) {
                specialty.setUserId(userId);
                specialty.setCreateTime(LocalDateTime.now());
                specialty.setUpdateTime(LocalDateTime.now());
            }
            specialtyService.saveBatch(specialtyList);
        }

        return userId;
    }

    @Override
    public Info getInfoByUserId(Long userId) {
        // 查询用户个人信息
        Profiles profiles = profilesService.getByUserId(userId);

        // 查询教育经历列表（按优先级降序）
        List<Edu> eduList = eduService.list(
                new LambdaQueryWrapper<Edu>()
                        .eq(Edu::getUserId, userId)
                        .orderByDesc(Edu::getPriority)
        );

        // 查询个人技能
        Skill skill = skillService.getOne(
                new LambdaQueryWrapper<Skill>()
                        .eq(Skill::getUserId, userId)
                        .last("LIMIT 1")
        );

        // 查询特长列表
        List<Specialty> specialtyList = specialtyService.list(
                new LambdaQueryWrapper<Specialty>()
                        .eq(Specialty::getUserId, userId)
        );

        // 查询工作经历列表（按优先级降序）
        List<Work> workList = workService.list(
                new LambdaQueryWrapper<Work>()
                        .eq(Work::getUserId, userId)
                        .orderByDesc(Work::getPriority)
        );

        return Info.builder()
                .profiles(profiles)
                .eduList(eduList)
                .skill(skill)
                .specialtyList(specialtyList)
                .workList(workList)
                .build();
    }
}