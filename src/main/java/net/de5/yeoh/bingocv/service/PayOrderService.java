package net.de5.yeoh.bingocv.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.de5.yeoh.bingocv.model.domain.PayOrder;
import net.de5.yeoh.bingocv.model.dto.OrderCreateRequest;
import net.de5.yeoh.bingocv.model.vo.PaymentResultVO;

import java.util.Map;

public interface PayOrderService extends IService<PayOrder> {
    PayOrder createOrder(Long userId, OrderCreateRequest request);

    PaymentResultVO initiatePay(Long userId, Long orderId, String payChannel);

    PayOrder close(Long userId, Long orderId);

    PayOrder detail(Long userId, Long orderId);

    PayOrder adminClose(Long operatorId, Long orderId, String reason);

    PayOrder adminRefund(Long operatorId, Long orderId, String reason, java.math.BigDecimal refundAmount);

    PayOrder updateAdminRemark(Long operatorId, Long orderId, String remark);

    Map<String, Object> myOrders(Long userId, Integer pageNum, Integer pageSize);

    Map<String, Object> adminOrders(Integer pageNum, Integer pageSize, String keyword, String status,
                                    String orderType, Long userId, String payChannel,
                                    String startTime, String endTime);
}
