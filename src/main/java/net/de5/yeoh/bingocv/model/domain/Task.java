package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@TableName(value = "bingo_task")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Task extends BaseDO implements Serializable {
    private String taskKey;
    private String name;
    private String taskType;
    private Integer rewardPoints;
    private String description;
    private Integer enabled;

    @TableField(exist = false)
    private Boolean completed;

    @TableField(exist = false)
    private Boolean rewarded;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
