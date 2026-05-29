package net.de5.yeoh.bingocv.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.api.Result;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.model.domain.Info;
import net.de5.yeoh.bingocv.service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 个人简历接口
 * @author: daChang
 * @date: 2026/5/11 11:40
 */
@RestController
@RequestMapping("/info")
@Slf4j
@Tag(name = "个人简历接口")
public class InfoController {
    @Autowired
    private InfoService infoService;

    @PostMapping("/submit")
    @CheckLogin
    @Operation(summary = "统一提交个人简历")
    public Result<Long> submit(@RequestBody Info info) {
        // 校验是否登录
        Long userId = UserContext.currentUserId();
        log.info("当前UserContext内容：{}", UserContext.currentUser());
        log.info("当前用户用户ID：{}", userId);
        if (userId == null) {
            throw new InfoException(InfoEnum.NOT_LOGIN);
        }

        // 参数校验
        if (info == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "简历信息不能为空");
        }

        return Result.ok(infoService.submit(info, userId));
    }

    @GetMapping
    @Operation(summary = "根据用户id查询个人简历")
    public Result<Info> getInfoByUserId(@RequestParam Long userid) {
        // 参数校验
        if (userid == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "用户ID不能为空");
        }

        return Result.ok(infoService.getInfoByUserId(userid));
    }
}
