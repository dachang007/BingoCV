package net.de5.yeoh.bingocv.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.de5.yeoh.bingocv.model.domain.AiInterviewQuestion;

import java.util.List;

public interface AiInterviewQuestionService extends IService<AiInterviewQuestion> {
    List<AiInterviewQuestion> listMine(Long userId, String masteryStatus);

    List<AiInterviewQuestion> generate(Long userId, String jobTitle, String jdContent, Integer count);

    AiInterviewQuestion updatePractice(Long userId, Long id, String masteryStatus, String practiceNote);
}
