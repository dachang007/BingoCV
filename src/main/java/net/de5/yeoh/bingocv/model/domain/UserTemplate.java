package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@TableName(value = "bingo_user_template")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserTemplate extends BaseDO implements Serializable {
    @TableField("userid")
    private Long userId;
    private Long templateId;
    private String source;
    private Integer active;

    @TableField(exist = false)
    private Template template;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
