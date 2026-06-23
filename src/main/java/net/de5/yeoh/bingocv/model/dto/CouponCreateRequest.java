package net.de5.yeoh.bingocv.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponCreateRequest {
    private String name;
    private String couponType;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private BigDecimal minAmount;
    private Long targetUserId;
    private Integer quantity;
    private LocalDateTime expireTime;
    private String remark;
}
