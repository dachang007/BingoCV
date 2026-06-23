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
import net.de5.yeoh.bingocv.model.domain.Edu;
import net.de5.yeoh.bingocv.service.EduService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教育经历控制器
 * 
 * 功能说明：
 * - 提供用户教育经历的CRUD操作
 * - 支持单条记录的增删改查
 * - 支持批量保存（先删除旧数据再批量插入）
 * - 所有接口均需要登录验证（除公开查询接口）
 * 
 * 数据权限：
 * - 用户只能访问和操作自己的教育经历数据
 * - 通过UserContext获取当前登录用户ID进行数据隔离
 */
@RestController
@RequestMapping("/edu")
@Slf4j
@Tag(name = "教育经历接口")
public class EduController {
    @Autowired
    private EduService eduService;

    /**
     * 获取当前用户的教育经历列表
     * 
     * 业务逻辑：
     * 1. 获取当前登录用户ID
     * 2. 查询该用户的所有教育经历记录
     * 3. 按优先级降序排序（优先级高的排在前面）
     * 4. 优先级相同时按更新时间降序排序
     * 
     * @return 教育经历列表（已排序）
     */
    @GetMapping("/list")
    @CheckLogin
    @Operation(summary = "获取当前用户教育经历")
    public Result<List<Edu>> list() {
        Long userId = requireUserId();
        return Result.ok(eduService.list(new LambdaQueryWrapper<Edu>()
                .eq(Edu::getUserId, userId)
                .orderByDesc(Edu::getPriority)
                .orderByDesc(Edu::getUpdateTime)));
    }

    @PostMapping
    @CheckLogin
    @Operation(summary = "新增教育经历")
    public Result<Edu> create(@RequestBody Edu edu) {
        Long userId = requireUserId();
        requireBody(edu, "教育经历不能为空");
        edu.setId(null);
        edu.setUserId(userId);
        if (edu.getPriority() == null) {
            edu.setPriority(0);
        }
        edu.setCreateTime(LocalDateTime.now());
        edu.setUpdateTime(LocalDateTime.now());
        eduService.save(edu);
        return Result.ok(edu);
    }

    @PutMapping("/{id}")
    @CheckLogin
    @Operation(summary = "更新教育经历")
    public Result<Edu> update(@PathVariable Long id, @RequestBody Edu edu) {
        Long userId = requireUserId();
        requireBody(edu, "教育经历不能为空");
        Edu existing = getOwnedEdu(id, userId);
        edu.setId(existing.getId());
        edu.setUserId(userId);
        edu.setCreateTime(existing.getCreateTime());
        edu.setUpdateTime(LocalDateTime.now());
        eduService.updateById(edu);
        return Result.ok(eduService.getById(id));
    }

    @DeleteMapping("/{id}")
    @CheckLogin
    @Operation(summary = "删除教育经历")
    public Result<Boolean> delete(@PathVariable Long id) {
        Long userId = requireUserId();
        getOwnedEdu(id, userId);
        return Result.ok(eduService.removeById(id));
    }

    @PostMapping("/insert")
    @CheckLogin
    @Operation(summary = "批量保存教育经历")
    public Result<List<Edu>> add(@RequestBody List<Edu> eduList) {
        Long userId = requireUserId();
        if (eduList == null || eduList.isEmpty()) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "教育经历列表不能为空");
        }
        eduList.forEach(edu -> edu.setUserId(userId));
        return Result.ok(eduService.add(eduList));
    }

    private Edu getOwnedEdu(Long id, Long userId) {
        if (id == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "ID不能为空");
        }
        Edu edu = eduService.getOne(new LambdaQueryWrapper<Edu>()
                .eq(Edu::getId, id)
                .eq(Edu::getUserId, userId));
        if (edu == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "教育经历不存在");
        }
        return edu;
    }

    private Long requireUserId() {
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new InfoException(InfoEnum.NOT_LOGIN);
        }
        return userId;
    }

    private void requireBody(Object body, String message) {
        if (body == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, message);
        }
    }
}
