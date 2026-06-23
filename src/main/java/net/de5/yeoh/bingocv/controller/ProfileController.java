package net.de5.yeoh.bingocv.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.api.Result;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.dto.ResumeData;
import net.de5.yeoh.bingocv.model.domain.Profiles;
import net.de5.yeoh.bingocv.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@Slf4j
@Tag(name = "个人信息接口")
public class ProfileController {

    @Autowired
    private ProfilesService profilesService;
    @Autowired
    private EduService eduService;
    @Autowired
    private WorkService workService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private SpecialtyService specialtyService;

    @GetMapping("/me")
    @CheckLogin
    @Operation(summary = "获取当前用户个人信息")
    public Result<Profiles> getMyProfile() {
        Long userId = requireUserId();
        Profiles profile = profilesService.getByUserId(userId);
        if (profile == null) {
            profile = profilesService.create(Profiles.builder().userId(userId).build());
        }
        return Result.ok(profile);
    }

    @PutMapping("/me")
    @CheckLogin
    @Operation(summary = "更新当前用户个人信息")
    public Result<Profiles> updateMyProfile(@RequestBody Profiles profile) {
        Long userId = requireUserId();
        if (profile == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "个人信息不能为空");
        }

        profile.setUserId(userId);
        Profiles existingProfile = profilesService.getByUserId(userId);
        if (existingProfile == null) {
            return Result.ok(profilesService.create(profile));
        }
        profile.setId(existingProfile.getId());
        return Result.ok(profilesService.updateProfile(profile));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取个人信息")
    public Result<Profiles> getProfileById(@PathVariable Long id) {
        if (id == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "ID不能为空");
        }

        Profiles profile = profilesService.getById(id);
        if (profile == null) {
            throw new InfoException(InfoEnum.PROFILE_NOT_EXISTS);
        }
        return Result.ok(profile);
    }

    @GetMapping("/me/full")
    @CheckLogin
    @Operation(summary = "获取当前用户完整简历数据")
    public Result<ResumeData> getMyFullResume() {
        Long userId = requireUserId();
        ResumeData resumeData = new ResumeData();
        resumeData.setProfile(profilesService.getByUserId(userId));
        resumeData.setEdu(eduService.listByUserId(userId));
        resumeData.setWork(workService.listByUserId(userId));
        resumeData.setSkill(skillService.getByUserId(userId));
        resumeData.setSpecialty(specialtyService.listByUserId(userId));
        return Result.ok(resumeData);
    }

    private Long requireUserId() {
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new InfoException(InfoEnum.NOT_LOGIN);
        }
        return userId;
    }
}
