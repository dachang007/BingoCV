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
public class AiJdMatchVO implements Serializable {
    private Integer matchScore;
    private String level;
    private String summary;
    private List<String> matchedKeywords;
    private List<String> missingKeywords;
    private List<String> strengths;
    private List<String> risks;
    private List<String> rewriteSuggestions;
    private List<String> interviewFocus;

    private static final long serialVersionUID = 1L;
}
