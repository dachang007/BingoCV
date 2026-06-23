package net.de5.yeoh.bingocv.service.impl;

import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.model.domain.PayOrder;
import net.de5.yeoh.bingocv.model.vo.PaymentResultVO;
import net.de5.yeoh.bingocv.service.PaymentChannelService;
import net.de5.yeoh.bingocv.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class PaymentChannelServiceImpl implements PaymentChannelService {

    private static final String CHANNEL_MOCK = "MOCK";
    private static final String CHANNEL_ALIPAY = "ALIPAY";
    private static final String CHANNEL_WECHAT = "WECHAT";

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public PaymentResultVO createPayment(PayOrder order, String channel) {
        String payChannel = normalizeChannel(channel);
        ensureChannelEnabled(payChannel);
        if (CHANNEL_MOCK.equals(payChannel)) {
            return PaymentResultVO.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .payChannel(payChannel)
                    .payStatus("PAID")
                    .paid(true)
                    .transactionId("MOCK" + System.currentTimeMillis())
                    .message("模拟支付成功")
                    .build();
        }

        // 真实支付后续只需要在这里对接 SDK/HTTP 下单，订单服务不再关心第三方细节。
        return PaymentResultVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .payChannel(payChannel)
                .payStatus("PENDING")
                .paid(false)
                .payUrl(buildPlaceholderPayUrl(payChannel, order))
                .message("支付通道已创建，请跳转第三方完成支付")
                .build();
    }

    @Override
    public PaymentResultVO handleCallback(String channel, Map<String, String> payload) {
        String payChannel = normalizeChannel(channel);
        ensureChannelEnabled(payChannel);
        // TODO 接入真实支付时在这里校验签名、幂等查询订单并回写支付状态。
        return PaymentResultVO.builder()
                .payChannel(payChannel)
                .payStatus("PENDING")
                .paid(false)
                .message("回调已接收，真实支付签名校验待接入")
                .build();
    }

    private String normalizeChannel(String channel) {
        return StringUtils.hasText(channel) ? channel.trim().toUpperCase() : CHANNEL_MOCK;
    }

    private void ensureChannelEnabled(String channel) {
        if (CHANNEL_MOCK.equals(channel)) {
            if (!systemConfigService.getBooleanValue("payment.mock.enabled", true)) {
                throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "模拟支付通道已关闭");
            }
            return;
        }
        if (CHANNEL_ALIPAY.equals(channel)) {
            if (!systemConfigService.getBooleanValue("payment.alipay.enabled", false)) {
                throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "支付宝支付暂未启用");
            }
            return;
        }
        if (CHANNEL_WECHAT.equals(channel)) {
            if (!systemConfigService.getBooleanValue("payment.wechat.enabled", false)) {
                throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "微信支付暂未启用");
            }
            return;
        }
        throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "支付渠道不正确");
    }

    private String buildPlaceholderPayUrl(String channel, PayOrder order) {
        return "/payment/pending?channel=" + channel + "&orderNo=" + order.getOrderNo();
    }
}
