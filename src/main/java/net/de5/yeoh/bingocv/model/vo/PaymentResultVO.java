package net.de5.yeoh.bingocv.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.de5.yeoh.bingocv.model.domain.PayOrder;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultVO implements Serializable {
    private Long orderId;
    private String orderNo;
    private String payChannel;
    private String payStatus;
    private Boolean paid;
    private String transactionId;
    private String payUrl;
    private String message;
    private PayOrder order;

    private static final long serialVersionUID = 1L;
}
