package net.de5.yeoh.bingocv.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.api.Result;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.model.domain.AiInterviewQuestion;
import net.de5.yeoh.bingocv.model.dto.AiInterviewGenerateRequest;
import net.de5.yeoh.bingocv.model.dto.AiInterviewUpdateRequest;
import net.de5.yeoh.bingocv.service.AiInterviewQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai/interview")
@Tag(name = "AI 面试练习接口")
public class AiInterviewQuestionController {

    @Autowired
    private AiInterviewQuestionService aiInterviewQuestionService;

    @GetMapping("/questions")
    @CheckLogin
    @Operation(summary = "查询我的面试题")
    public Result<List<AiInterviewQuestion>> list(@RequestParam(required = false) String masteryStatus) {
        return Result.ok(aiInterviewQuestionService.listMine(requireUserId(), masteryStatus));
    }

    @PostMapping("/questions/generate")
    @CheckLogin
    @Operation(summary = "生成面试题并保存")
    public Result<List<AiInterviewQuestion>> generate(@RequestBody AiInterviewGenerateRequest request) {
        if (request == null) {
            request = new AiInterviewGenerateRequest();
        }
        return Result.ok(aiInterviewQuestionService.generate(
                requireUserId(), request.getJobTitle(), request.getJdContent(), request.getCount()));
    }

    @PutMapping("/questions/{id}")
    @CheckLogin
    @Operation(summary = "更新面试题练习状态")
    public Result<AiInterviewQuestion> update(@PathVariable Long id, @RequestBody AiInterviewUpdateRequest request) {
        if (request == null) {
            request = new AiInterviewUpdateRequest();
        }
        return Result.ok(aiInterviewQuestionService.updatePractice(
                requireUserId(), id, request.getMasteryStatus(), request.getPracticeNote()));
    }

    @DeleteMapping("/questions/{id}")
    @CheckLogin
    @Operation(summary = "删除面试题")
    public Result<Boolean> delete(@PathVariable Long id) {
        Long userId = requireUserId();
        AiInterviewQuestion question = aiInterviewQuestionService.getById(id);
        if (question == null || !userId.equals(question.getUserId())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "面试题不存在");
        }
        return Result.ok(aiInterviewQuestionService.removeById(id));
    }

    private Long requireUserId() {
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new InfoException(InfoEnum.NOT_LOGIN);
        }
        return userId;
    }
}
