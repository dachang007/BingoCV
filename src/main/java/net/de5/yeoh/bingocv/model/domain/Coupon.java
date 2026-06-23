package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName(value = "bingo_coupon")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon extends BaseDO implements Serializable {
    private String couponCode;
    private String name;
    private String couponType;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private BigDecimal minAmount;

    @TableField("target_userid")
    private Long targetUserId;

    private String status;
    private LocalDateTime expireTime;
    private LocalDateTime usedTime;
    private String remark;

    private static final long serialVersionUID = 1L;
}
