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

@TableName(value = "bingo_pay_refund")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PayRefund extends BaseDO implements Serializable {
    private Long orderId;
    private String orderNo;

    @TableField("userid")
    private Long userId;

    private BigDecimal refundAmount;
    private String refundStatus;
    private String refundNo;
    private String reason;
    private Long operatorId;
    private LocalDateTime refundTime;

    private static final long serialVersionUID = 1L;
}
