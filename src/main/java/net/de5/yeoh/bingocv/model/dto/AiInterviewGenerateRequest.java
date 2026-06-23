package net.de5.yeoh.bingocv.model.dto;

import lombok.Data;

@Data
public class AiInterviewGenerateRequest {
    private String jobTitle;
    private String jdContent;
    private Integer count;
}
