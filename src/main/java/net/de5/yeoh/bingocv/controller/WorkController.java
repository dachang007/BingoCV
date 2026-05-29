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
import net.de5.yeoh.bingocv.model.domain.Work;
import net.de5.yeoh.bingocv.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/work")
@Slf4j
@Tag(name = "工作经历接口")
public class WorkController {
    @Autowired
    private WorkService workService;

    @GetMapping("/list")
    @CheckLogin
    @Operation(summary = "获取当前用户工作经历")
    public Result<List<Work>> list() {
        Long userId = requireUserId();
        return Result.ok(workService.list(new LambdaQueryWrapper<Work>()
                .eq(Work::getUserId, userId)
                .orderByDesc(Work::getPriority)
                .orderByDesc(Work::getUpdateTime)));
    }

    @PostMapping
    @CheckLogin
    @Operation(summary = "新增工作经历")
    public Result<Work> create(@RequestBody Work work) {
        Long userId = requireUserId();
        requireBody(work, "工作经历不能为空");
        work.setId(null);
        work.setUserId(userId);
        if (work.getPriority() == null) {
            work.setPriority(0);
        }
        work.setCreateTime(LocalDateTime.now());
        work.setUpdateTime(LocalDateTime.now());
        workService.save(work);
        return Result.ok(work);
    }

    @PutMapping("/{id}")
    @CheckLogin
    @Operation(summary = "更新工作经历")
    public Result<Work> update(@PathVariable Long id, @RequestBody Work work) {
        Long userId = requireUserId();
        requireBody(work, "工作经历不能为空");
        Work existing = getOwnedWork(id, userId);
        work.setId(existing.getId());
        work.setUserId(userId);
        work.setCreateTime(existing.getCreateTime());
        work.setUpdateTime(LocalDateTime.now());
        workService.updateById(work);
        return Result.ok(workService.getById(id));
    }

    @DeleteMapping("/{id}")
    @CheckLogin
    @Operation(summary = "删除工作经历")
    public Result<Boolean> delete(@PathVariable Long id) {
        Long userId = requireUserId();
        getOwnedWork(id, userId);
        return Result.ok(workService.removeById(id));
    }

    @PostMapping("/insert")
    @CheckLogin
    @Operation(summary = "批量保存工作经历")
    public Result<List<Work>> add(@RequestBody List<Work> workList) {
        Long userId = requireUserId();
        if (workList == null || workList.isEmpty()) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "工作经历列表不能为空");
        }
        workList.forEach(work -> work.setUserId(userId));
        return Result.ok(workService.add(workList));
    }

    private Work getOwnedWork(Long id, Long userId) {
        if (id == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "ID不能为空");
        }
        Work work = workService.getOne(new LambdaQueryWrapper<Work>()
                .eq(Work::getId, id)
                .eq(Work::getUserId, userId));
        if (work == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "工作经历不存在");
        }
        return work;
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
