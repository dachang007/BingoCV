package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName(value = "bingo_points")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Points extends BaseDO implements Serializable {
    /**
     * 用户ID
     */
    @TableField("userid")
    private Long userId;

    /**
     * 积分余额
     */
    private Integer balance;

    /**
     * 积分总和
     */
    private Integer totalEarned;

    /**
     * 积分总支出
     */
    private Integer totalSpent;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
