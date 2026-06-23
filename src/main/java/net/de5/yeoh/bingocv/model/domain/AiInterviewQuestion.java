package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@TableName(value = "bingo_ai_interview_question")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AiInterviewQuestion extends BaseDO implements Serializable {
    @TableField("userid")
    private Long userId;
    private String jobTitle;
    private String category;
    private String difficulty;
    private String question;
    private String answerHint;
    private String source;
    private String masteryStatus;
    private String practiceNote;

    private static final long serialVersionUID = 1L;
}
