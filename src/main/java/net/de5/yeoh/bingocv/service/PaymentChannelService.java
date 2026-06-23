package net.de5.yeoh.bingocv.service;

import net.de5.yeoh.bingocv.model.domain.PayOrder;
import net.de5.yeoh.bingocv.model.vo.PaymentResultVO;

import java.util.Map;

public interface PaymentChannelService {
    PaymentResultVO createPayment(PayOrder order, String channel);

    PaymentResultVO handleCallback(String channel, Map<String, String> payload);
}
