package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@TableName(value = "bingo_points_log")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PointsLog extends BaseDO implements Serializable {
    @TableField("userid")
    private Long userId;
    private String bizType;
    private Long bizId;
    private Integer amount;
    private Integer balance;
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
