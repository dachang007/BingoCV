package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 用于描述用户的工作经历
 * @TableName bingo_work
 */
@TableName(value ="bingo_work")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Work extends BaseDO implements Serializable {
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
     * 所在公司
     */
    private String company;

    /**
     * 岗位名称
     */
    private String job;

    /**
     * 工作描述
     */
    private String description;

    /**
     * 优先级
     */
    private Integer priority;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
