package net.de5.yeoh.bingocv.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.api.Result;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.model.domain.Template;
import net.de5.yeoh.bingocv.model.domain.UserTemplate;
import net.de5.yeoh.bingocv.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/template")
@Tag(name = "简历模板接口")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @GetMapping("/market")
    @Operation(summary = "模板市场")
    public Result<List<Template>> market(@RequestParam(required = false) String industry,
                                         @RequestParam(required = false) String type) {
        return Result.ok(templateService.market(industry, type, UserContext.currentUserId()));
    }

    @GetMapping("/mine")
    @CheckLogin
    @Operation(summary = "我的模板")
    public Result<List<UserTemplate>> mine() {
        return Result.ok(templateService.myTemplates(requireUserId()));
    }

    @PostMapping("/{templateId}/acquire")
    @CheckLogin
    @Operation(summary = "领取或兑换模板")
    public Result<UserTemplate> acquire(@PathVariable Long templateId) {
        return Result.ok(templateService.acquire(requireUserId(), templateId));
    }

    @PostMapping("/{templateId}/activate")
    @CheckLogin
    @Operation(summary = "启用模板")
    public Result<UserTemplate> activate(@PathVariable Long templateId) {
        return Result.ok(templateService.activate(requireUserId(), templateId));
    }

    private Long requireUserId() {
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new InfoException(InfoEnum.NOT_LOGIN);
        }
        return userId;
    }
}
