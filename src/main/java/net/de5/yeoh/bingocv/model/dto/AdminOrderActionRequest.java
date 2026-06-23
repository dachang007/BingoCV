package net.de5.yeoh.bingocv.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminOrderActionRequest {
    private String reason;
    private String remark;
    private BigDecimal refundAmount;
}
