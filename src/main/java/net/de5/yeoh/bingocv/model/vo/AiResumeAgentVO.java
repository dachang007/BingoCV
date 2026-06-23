package net.de5.yeoh.bingocv.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResumeAgentVO implements Serializable {
    private Integer readiness;
    private String stage;
    private String summary;
    private List<AgentAction> actions;
    private List<String> reminders;

    private static final long serialVersionUID = 1L;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentAction implements Serializable {
        private String title;
        private String description;
        private String priority;
        private String reason;
        private String expectedImpact;
        private String routeName;
        private String buttonText;

        private static final long serialVersionUID = 1L;
    }
}
