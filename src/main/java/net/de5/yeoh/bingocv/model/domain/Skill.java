package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 用户技能表
 * @TableName bingo_skill
 */
@TableName(value ="bingo_skill")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Skill extends BaseDO implements Serializable {
    /**
     * 用户编号
     */
    @TableField("userid")
    private Long userId;

    /**
     * 用户的技能词列表
     */
    private String keywords;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
