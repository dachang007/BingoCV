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

/**
 * 技能控制器
 * 
 * 功能说明：
 * - 提供用户技能信息的管理接口
 * - 每个用户只有一条技能记录
 * - 技能关键词以逗号分隔存储
 * - 所有接口均需要登录验证
 * 
 * 数据权限：
 * - 用户只能访问和操作自己的技能数据
 * - 通过UserContext获取当前登录用户ID进行数据隔离
 */
@RestController
@RequestMapping("/skill")
@Slf4j
@Tag(name = "技能接口")
public class SkillController {
    @Autowired
    private SkillService skillService;

    /**
     * 获取当前用户的技能信息
     * 
     * 业务逻辑：
     * 1. 获取当前登录用户ID
     * 2. 查询该用户的技能记录（每个用户最多一条）
     * 3. 如果不存在，返回空技能对象（keywords为空字符串）
     * 
     * @return 技能信息对象
     */
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
