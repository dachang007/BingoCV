package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@TableName(value = "bingo_ai_usage_log")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AiUsageLog extends BaseDO implements Serializable {
    @TableField("userid")
    private Long userId;
    private String actionType;
    private String provider;
    private String model;
    private Integer success;
    private Integer fallbackUsed;
    private Long costMs;
    private String errorMsg;

    private static final long serialVersionUID = 1L;
}
