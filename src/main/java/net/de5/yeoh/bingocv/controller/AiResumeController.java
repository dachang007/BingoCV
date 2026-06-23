package net.de5.yeoh.bingocv.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.api.Result;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.model.dto.AiJdMatchRequest;
import net.de5.yeoh.bingocv.model.dto.AiPolishRequest;
import net.de5.yeoh.bingocv.model.vo.AiJdMatchVO;
import net.de5.yeoh.bingocv.model.vo.AiResumeAgentVO;
import net.de5.yeoh.bingocv.model.vo.AiResumeAnalysisVO;
import net.de5.yeoh.bingocv.service.AiResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/resume")
@Tag(name = "AI 简历增强接口")
public class AiResumeController {

    @Autowired
    private AiResumeService aiResumeService;

    @GetMapping("/analyze")
    @CheckLogin
    @Operation(summary = "分析当前用户简历")
    public Result<AiResumeAnalysisVO> analyze() {
        return Result.ok(aiResumeService.analyze(requireUserId()));
    }

    @GetMapping("/agent-plan")
    @CheckLogin
    @Operation(summary = "生成 AI Agent 行动计划")
    public Result<AiResumeAgentVO> agentPlan() {
        return Result.ok(aiResumeService.agentPlan(requireUserId()));
    }

    @PostMapping("/jd-match")
    @CheckLogin
    @Operation(summary = "分析简历与 JD 匹配度")
    public Result<AiJdMatchVO> jdMatch(@RequestBody AiJdMatchRequest request) {
        return Result.ok(aiResumeService.matchJd(requireUserId(), request.getJobTitle(), request.getJdContent()));
    }

    @PostMapping("/polish")
    @CheckLogin
    @Operation(summary = "润色简历文本")
    public Result<String> polish(@RequestBody AiPolishRequest request) {
        return Result.ok(aiResumeService.polish(requireUserId(), request.getScene(), request.getContent()));
    }

    private Long requireUserId() {
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new InfoException(InfoEnum.NOT_LOGIN);
        }
        return userId;
    }
}
