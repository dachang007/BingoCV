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
import net.de5.yeoh.bingocv.model.domain.Specialty;
import net.de5.yeoh.bingocv.service.SpecialtyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/specialty")
@Slf4j
@Tag(name = "特长接口")
public class SpecialtyController {
    @Autowired
    private SpecialtyService specialtyService;

    @GetMapping("/list")
    @CheckLogin
    @Operation(summary = "获取当前用户特长")
    public Result<List<Specialty>> list() {
        Long userId = requireUserId();
        return Result.ok(specialtyService.list(new LambdaQueryWrapper<Specialty>()
                .eq(Specialty::getUserId, userId)
                .orderByDesc(Specialty::getUpdateTime)));
    }

    @PostMapping
    @CheckLogin
    @Operation(summary = "新增特长")
    public Result<Specialty> create(@RequestBody Specialty specialty) {
        Long userId = requireUserId();
        requireBody(specialty, "特长不能为空");
        specialty.setId(null);
        specialty.setUserId(userId);
        specialty.setCreateTime(LocalDateTime.now());
        specialty.setUpdateTime(LocalDateTime.now());
        specialtyService.save(specialty);
        return Result.ok(specialty);
    }

    @PutMapping("/{id}")
    @CheckLogin
    @Operation(summary = "更新特长")
    public Result<Specialty> update(@PathVariable Long id, @RequestBody Specialty specialty) {
        Long userId = requireUserId();
        requireBody(specialty, "特长不能为空");
        Specialty existing = getOwnedSpecialty(id, userId);
        specialty.setId(existing.getId());
        specialty.setUserId(userId);
        specialty.setCreateTime(existing.getCreateTime());
        specialty.setUpdateTime(LocalDateTime.now());
        specialtyService.updateById(specialty);
        return Result.ok(specialtyService.getById(id));
    }

    @DeleteMapping("/{id}")
    @CheckLogin
    @Operation(summary = "删除特长")
    public Result<Boolean> delete(@PathVariable Long id) {
        Long userId = requireUserId();
        getOwnedSpecialty(id, userId);
        return Result.ok(specialtyService.removeById(id));
    }

    @PostMapping("/insert")
    @CheckLogin
    @Operation(summary = "批量保存特长")
    public Result<List<Specialty>> add(@RequestBody List<Specialty> specialtyList) {
        Long userId = requireUserId();
        if (specialtyList == null || specialtyList.isEmpty()) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "特长列表不能为空");
        }
        specialtyList.forEach(specialty -> specialty.setUserId(userId));
        return Result.ok(specialtyService.add(specialtyList));
    }

    private Specialty getOwnedSpecialty(Long id, Long userId) {
        if (id == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "ID不能为空");
        }
        Specialty specialty = specialtyService.getOne(new LambdaQueryWrapper<Specialty>()
                .eq(Specialty::getId, id)
                .eq(Specialty::getUserId, userId));
        if (specialty == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "特长不存在");
        }
        return specialty;
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
