package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 用于描述用户的学习
 * @TableName bingo_edu
 */
@TableName(value ="bingo_edu")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Edu extends BaseDO implements Serializable {
    /**
     * 用户编号
     */
    @TableField("userid")
    private Long userId;

    /**
     * 开始时间
     */
    private String start;

    /**
     * 结束时间
     */
    private String end;

    /**
     * 学校
     */
    private String school;

    /**
     * 专业名称
     */
    private String study;

    /**
     * 描述
     */
    private String description;

    /**
     * 优先级
     */
    private Integer priority;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
