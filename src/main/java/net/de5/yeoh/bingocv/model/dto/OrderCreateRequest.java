package net.de5.yeoh.bingocv.model.dto;

import lombok.Data;

@Data
public class OrderCreateRequest {
    private String orderType;
    private Long relatedId;
    private Integer points;
    private String couponCode;
    private String payChannel;
}
