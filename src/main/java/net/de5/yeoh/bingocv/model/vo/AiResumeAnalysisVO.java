package net.de5.yeoh.bingocv.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiResumeAnalysisVO {
    private Integer score;
    private String level;
    private String summary;
    private List<String> highlights;
    private List<String> problems;
    private List<String> suggestions;
    private List<String> polishedBullets;
    private List<String> interviewQuestions;
    private List<SectionScore> sectionScores;

    @Data
    @Builder
    public static class SectionScore {
        private String name;
        private Integer score;
        private String comment;
    }
}
