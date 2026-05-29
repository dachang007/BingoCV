package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;

@TableName(value = "bingo_sign_in")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SignIn extends BaseDO implements Serializable {
    @TableField("userid")
    private Long userId;
    private LocalDate signDate;
    private Integer streakDays;
    private Integer rewardPoints;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
