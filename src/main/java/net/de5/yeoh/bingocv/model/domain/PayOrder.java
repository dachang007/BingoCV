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

@TableName(value = "bingo_pay_order")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrder extends BaseDO implements Serializable {
    private String orderNo;

    @TableField("userid")
    private Long userId;

    private String orderType;
    private Long relatedId;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal amount;
    private Integer points;
    private Long couponId;
    private String payChannel;
    private String payStatus;
    private String transactionId;
    private LocalDateTime paidTime;
    private LocalDateTime refundTime;
    private String adminRemark;

    private static final long serialVersionUID = 1L;
}
