package net.de5.yeoh.bingocv.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.api.Result;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.model.domain.Skill;
import net.de5.yeoh.bingocv.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skill")
@Slf4j
@Tag(name = "技能接口")
public class SkillController {
    @Autowired
    private SkillService skillService;

    @GetMapping("/me")
    @CheckLogin
    @Operation(summary = "获取当前用户技能")
    public Result<Skill> getMySkill() {
        Long userId = requireUserId();
        Skill skill = skillService.getOne(new LambdaQueryWrapper<Skill>()
                .eq(Skill::getUserId, userId)
                .last("LIMIT 1"));
        if (skill == null) {
            skill = Skill.builder().userId(userId).keywords("").build();
        }
        return Result.ok(skill);
    }

    @PutMapping("/me")
    @CheckLogin
    @Operation(summary = "保存当前用户技能")
    public Result<Skill> updateMySkill(@RequestBody Skill skill) {
        return add(skill);
    }

    @PostMapping("/insert")
    @CheckLogin
    @Operation(summary = "保存技能")
    public Result<Skill> add(@RequestBody Skill skill) {
        Long userId = requireUserId();
        if (skill == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "技能信息不能为空");
        }
        skill.setUserId(userId);
        return Result.ok(skillService.add(skill));
    }

    private Long requireUserId() {
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new InfoException(InfoEnum.NOT_LOGIN);
        }
        return userId;
    }
}
