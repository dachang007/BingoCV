package net.de5.yeoh.bingocv.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.mapper.AiInterviewQuestionMapper;
import net.de5.yeoh.bingocv.model.domain.AiInterviewQuestion;
import net.de5.yeoh.bingocv.service.AiInterviewQuestionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiInterviewQuestionServiceImpl extends ServiceImpl<AiInterviewQuestionMapper, AiInterviewQuestion>
        implements AiInterviewQuestionService {

    @Override
    public List<AiInterviewQuestion> listMine(Long userId, String masteryStatus) {
        LambdaQueryWrapper<AiInterviewQuestion> wrapper = new LambdaQueryWrapper<AiInterviewQuestion>()
                .eq(AiInterviewQuestion::getUserId, userId)
                .orderByDesc(AiInterviewQuestion::getCreateTime);
        if (StringUtils.hasText(masteryStatus)) {
            wrapper.eq(AiInterviewQuestion::getMasteryStatus, masteryStatus);
        }
        return list(wrapper);
    }

    @Override
    public List<AiInterviewQuestion> generate(Long userId, String jobTitle, String jdContent, Integer count) {
        int size = count == null ? 8 : Math.max(3, Math.min(count, 15));
        List<AiInterviewQuestion> questions = buildLocalQuestions(userId, jobTitle, jdContent, size);
        saveBatch(questions);
        return questions;
    }

    @Override
    public AiInterviewQuestion updatePractice(Long userId, Long id, String masteryStatus, String practiceNote) {
        if (id == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "题目 ID 不能为空");
        }
        AiInterviewQuestion question = getOne(new LambdaQueryWrapper<AiInterviewQuestion>()
                .eq(AiInterviewQuestion::getId, id)
                .eq(AiInterviewQuestion::getUserId, userId));
        if (question == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "面试题不存在");
        }
        question.setMasteryStatus(StrUtil.blankToDefault(masteryStatus, question.getMasteryStatus()));
        question.setPracticeNote(practiceNote);
        updateById(question);
        return getById(id);
    }

    private List<AiInterviewQuestion> buildLocalQuestions(Long userId, String jobTitle, String jdContent, int count) {
        String target = StrUtil.blankToDefault(jobTitle, "目标岗位");
        List<AiInterviewQuestion> questions = new ArrayList<>();
        add(questions, userId, target, "自我介绍", "EASY",
                "请用 2 分钟介绍自己，并说明你为什么适合“" + target + "”。",
                "建议按背景、核心技能、代表项目、求职目标四段组织。");
        add(questions, userId, target, "项目经历", "MEDIUM",
                "挑一个最能代表你能力的项目，讲清楚背景、职责、难点和结果。",
                "重点准备可量化结果，以及你个人负责的具体部分。");
        add(questions, userId, target, "项目经历", "MEDIUM",
                "项目中遇到过什么技术或协作难点？你是如何推进解决的？",
                "用 STAR 法回答：场景、任务、行动、结果。");
        add(questions, userId, target, "能力验证", "HARD",
                "如果重新做你简历里的一个项目，你会优先优化哪个环节？为什么？",
                "体现复盘能力，避免只说工具，最好结合指标或业务影响。");
        add(questions, userId, target, "岗位匹配", "MEDIUM",
                "这份岗位 JD 中哪些要求与你最匹配？请举例说明。",
                "提前把 JD 关键词和简历项目逐一对应。");
        add(questions, userId, target, "岗位匹配", "MEDIUM",
                "你目前和岗位要求相比还有哪些短板？准备如何补齐？",
                "诚实回答，不要否认短板，强调学习计划和已采取行动。");

        if (StringUtils.hasText(jdContent)) {
            add(questions, userId, target, "JD 深挖", "HARD",
                    "请结合 JD 中的核心职责，说明你过往经历里最接近的一次实践。",
                    "从 JD 里挑 1-2 个关键词，映射到真实项目或工作经历。");
            add(questions, userId, target, "JD 深挖", "HARD",
                    "如果入职后第一个月要完成 JD 中的核心任务，你会如何拆解计划？",
                    "回答里体现目标拆解、沟通协作、风险识别和交付节奏。");
        }

        while (questions.size() < count) {
            add(questions, userId, target, "通用问题", "EASY",
                    "你希望下一份工作重点提升哪项能力？为什么？",
                    "结合职业规划回答，避免泛泛而谈。");
        }
        return questions.stream().limit(count).toList();
    }

    private void add(List<AiInterviewQuestion> list, Long userId, String jobTitle, String category,
                     String difficulty, String question, String answerHint) {
        list.add(AiInterviewQuestion.builder()
                .userId(userId)
                .jobTitle(jobTitle)
                .category(category)
                .difficulty(difficulty)
                .question(question)
                .answerHint(answerHint)
                .source("LOCAL")
                .masteryStatus("TODO")
                .practiceNote("")
                .build());
    }
}
