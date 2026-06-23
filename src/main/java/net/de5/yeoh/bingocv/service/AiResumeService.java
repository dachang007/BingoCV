package net.de5.yeoh.bingocv.service;

import net.de5.yeoh.bingocv.model.vo.AiJdMatchVO;
import net.de5.yeoh.bingocv.model.vo.AiResumeAgentVO;
import net.de5.yeoh.bingocv.model.vo.AiResumeAnalysisVO;

public interface AiResumeService {
    AiResumeAnalysisVO analyze(Long userId);

    AiResumeAgentVO agentPlan(Long userId);

    AiJdMatchVO matchJd(Long userId, String jobTitle, String jdContent);

    String polish(Long userId, String scene, String content);
}
