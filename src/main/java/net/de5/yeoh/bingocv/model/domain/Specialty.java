package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 用于描述用户特长
 * @TableName bingo_specialty
 */
@TableName(value ="bingo_specialty")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Specialty extends BaseDO implements Serializable {
    /**
     * 用户编号
     */
    @TableField("userid")
    private Long userId;

    /**
     * 特长名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
