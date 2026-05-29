package net.de5.yeoh.bingocv.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.api.Result;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.model.domain.PointsLog;
import net.de5.yeoh.bingocv.model.domain.SignIn;
import net.de5.yeoh.bingocv.model.domain.Task;
import net.de5.yeoh.bingocv.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/points")
@Tag(name = "积分中心接口")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @GetMapping("/dashboard")
    @CheckLogin
    @Operation(summary = "积分中心概览")
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(pointsService.dashboard(requireUserId()));
    }

    @GetMapping("/logs")
    @CheckLogin
    @Operation(summary = "积分流水")
    public Result<List<PointsLog>> logs() {
        return Result.ok(pointsService.logs(requireUserId()));
    }

    @GetMapping("/tasks")
    @CheckLogin
    @Operation(summary = "任务列表")
    public Result<List<Task>> tasks() {
        return Result.ok(pointsService.tasks(requireUserId()));
    }

    @PostMapping("/sign-in")
    @CheckLogin
    @Operation(summary = "每日签到")
    public Result<SignIn> signIn() {
        return Result.ok(pointsService.signIn(requireUserId()));
    }

    private Long requireUserId() {
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new InfoException(InfoEnum.NOT_LOGIN);
        }
        return userId;
    }
}
