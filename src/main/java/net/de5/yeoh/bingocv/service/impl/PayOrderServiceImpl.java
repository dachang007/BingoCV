package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.mapper.PayOrderMapper;
import net.de5.yeoh.bingocv.model.domain.Coupon;
import net.de5.yeoh.bingocv.model.domain.PayOrder;
import net.de5.yeoh.bingocv.model.domain.PayRefund;
import net.de5.yeoh.bingocv.model.domain.Template;
import net.de5.yeoh.bingocv.model.domain.UserTemplate;
import net.de5.yeoh.bingocv.model.dto.OrderCreateRequest;
import net.de5.yeoh.bingocv.model.vo.PaymentResultVO;
import net.de5.yeoh.bingocv.service.CouponService;
import net.de5.yeoh.bingocv.service.PayOrderService;
import net.de5.yeoh.bingocv.service.PayRefundService;
import net.de5.yeoh.bingocv.service.PaymentChannelService;
import net.de5.yeoh.bingocv.service.PointsService;
import net.de5.yeoh.bingocv.service.TemplateService;
import net.de5.yeoh.bingocv.service.UserTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements PayOrderService {

    private static final String TYPE_POINTS = "POINTS";
    private static final String TYPE_TEMPLATE = "TEMPLATE";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PAID = "PAID";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final String STATUS_REFUNDED = "REFUNDED";

    @Autowired
    private PointsService pointsService;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private UserTemplateService userTemplateService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private PaymentChannelService paymentChannelService;
    @Autowired
    private PayRefundService payRefundService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrder createOrder(Long userId, OrderCreateRequest request) {
        if (request == null || !StringUtils.hasText(request.getOrderType())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "请选择订单类型");
        }
        String orderType = request.getOrderType().toUpperCase();
        BigDecimal originalAmount = calculateAmount(orderType, request);
        Coupon coupon = resolveCoupon(userId, request.getCouponCode(), originalAmount);
        BigDecimal discountAmount = calculateDiscount(originalAmount, coupon);
        BigDecimal amount = originalAmount.subtract(discountAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        PayOrder order = PayOrder.builder()
                .orderNo(generateOrderNo())
                .userId(userId)
                .orderType(orderType)
                .relatedId(TYPE_TEMPLATE.equals(orderType) ? request.getRelatedId() : null)
                .originalAmount(originalAmount)
                .discountAmount(discountAmount)
                .amount(amount)
                .points(TYPE_POINTS.equals(orderType) ? request.getPoints() : 0)
                .couponId(coupon == null ? null : coupon.getId())
                .payChannel(StringUtils.hasText(request.getPayChannel()) ? request.getPayChannel().toUpperCase() : "MOCK")
                .payStatus(STATUS_PENDING)
                .build();
        this.save(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultVO initiatePay(Long userId, Long orderId, String payChannel) {
        PayOrder order = getOwnedOrder(userId, orderId);
        if (!STATUS_PENDING.equals(order.getPayStatus())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "当前订单状态不允许支付");
        }

        PaymentResultVO payment = paymentChannelService.createPayment(order, payChannel);
        order.setPayChannel(payment.getPayChannel());
        if (Boolean.TRUE.equals(payment.getPaid())) {
            completePaidOrder(userId, order, payment.getTransactionId());
            payment.setPayStatus(STATUS_PAID);
            payment.setOrder(order);
            return payment;
        }
        this.updateById(order);
        payment.setOrder(order);
        return payment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrder close(Long userId, Long orderId) {
        PayOrder order = getOwnedOrder(userId, orderId);
        closePendingOrder(order, "用户关闭订单");
        return order;
    }

    @Override
    public PayOrder detail(Long userId, Long orderId) {
        return getOwnedOrder(userId, orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrder adminClose(Long operatorId, Long orderId, String reason) {
        PayOrder order = getRequiredOrder(orderId);
        closePendingOrder(order, StringUtils.hasText(reason) ? reason : "管理员关闭订单");
        order.setAdminRemark(appendRemark(order.getAdminRemark(), operatorId, "关闭订单", reason));
        this.updateById(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrder adminRefund(Long operatorId, Long orderId, String reason, BigDecimal refundAmount) {
        PayOrder order = getRequiredOrder(orderId);
        if (!STATUS_PAID.equals(order.getPayStatus())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "只有已支付订单可以退款");
        }
        BigDecimal amount = refundAmount == null ? order.getAmount() : refundAmount;
        if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(order.getAmount()) > 0) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "退款金额不正确");
        }

        // 当前先记录退款业务动作；真实接支付平台后再在回调里确认退款成功。
        PayRefund refund = PayRefund.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .refundAmount(amount.setScale(2, RoundingMode.HALF_UP))
                .refundStatus("SUCCESS")
                .refundNo("RFD" + System.currentTimeMillis())
                .reason(StringUtils.hasText(reason) ? reason : "管理员退款")
                .operatorId(operatorId)
                .refundTime(LocalDateTime.now())
                .build();
        payRefundService.save(refund);

        order.setPayStatus(STATUS_REFUNDED);
        order.setRefundTime(refund.getRefundTime());
        order.setAdminRemark(appendRemark(order.getAdminRemark(), operatorId, "退款", refund.getReason()));
        this.updateById(order);
        return order;
    }

    @Override
    public PayOrder updateAdminRemark(Long operatorId, Long orderId, String remark) {
        PayOrder order = getRequiredOrder(orderId);
        order.setAdminRemark(appendRemark(order.getAdminRemark(), operatorId, "备注", remark));
        this.updateById(order);
        return order;
    }

    @Override
    public Map<String, Object> myOrders(Long userId, Integer pageNum, Integer pageSize) {
        Page<PayOrder> page = this.page(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getUserId, userId)
                .orderByDesc(PayOrder::getCreateTime));
        return pageResult(page, pageNum, pageSize);
    }

    @Override
    public Map<String, Object> adminOrders(Integer pageNum, Integer pageSize, String keyword, String status,
                                           String orderType, Long userId, String payChannel,
                                           String startTime, String endTime) {
        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<PayOrder>()
                .orderByDesc(PayOrder::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(query -> query.like(PayOrder::getOrderNo, keyword)
                    .or()
                    .like(PayOrder::getTransactionId, keyword)
                    .or()
                    .like(PayOrder::getAdminRemark, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PayOrder::getPayStatus, status.toUpperCase());
        }
        if (StringUtils.hasText(orderType)) {
            wrapper.eq(PayOrder::getOrderType, orderType.toUpperCase());
        }
        if (userId != null) {
            wrapper.eq(PayOrder::getUserId, userId);
        }
        if (StringUtils.hasText(payChannel)) {
            wrapper.eq(PayOrder::getPayChannel, payChannel.toUpperCase());
        }
        parseTime(startTime, false).ifPresent(time -> wrapper.ge(PayOrder::getCreateTime, time));
        parseTime(endTime, true).ifPresent(time -> wrapper.le(PayOrder::getCreateTime, time));
        Page<PayOrder> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return pageResult(page, pageNum, pageSize);
    }

    private Map<String, Object> pageResult(Page<PayOrder> page, Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    private void completePaidOrder(Long userId, PayOrder order, String transactionId) {
        markCouponUsed(order);
        if (TYPE_POINTS.equals(order.getOrderType())) {
            pointsService.change(userId, order.getPoints(), "POINTS_RECHARGE", order.getId(), "积分充值订单：" + order.getOrderNo());
        } else if (TYPE_TEMPLATE.equals(order.getOrderType())) {
            grantTemplate(userId, order.getRelatedId());
        }
        order.setPayStatus(STATUS_PAID);
        order.setTransactionId(transactionId);
        order.setPaidTime(LocalDateTime.now());
        this.updateById(order);
    }

    private void closePendingOrder(PayOrder order, String reason) {
        if (!STATUS_PENDING.equals(order.getPayStatus())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "只能关闭待支付订单");
        }
        releaseCoupon(order);
        order.setPayStatus(STATUS_CLOSED);
        order.setAdminRemark(appendRemark(order.getAdminRemark(), null, "关闭订单", reason));
        this.updateById(order);
    }

    private BigDecimal calculateAmount(String orderType, OrderCreateRequest request) {
        if (TYPE_POINTS.equals(orderType)) {
            int points = request.getPoints() == null ? 0 : request.getPoints();
            if (points <= 0 || points > 100000) {
                throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "积分数量不正确");
            }
            return BigDecimal.valueOf(points).divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
        }
        if (TYPE_TEMPLATE.equals(orderType)) {
            Template template = templateService.getById(request.getRelatedId());
            if (template == null || !"PAID".equals(template.getTemplateType())) {
                throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "付费模板不存在");
            }
            return template.getPrice() == null ? BigDecimal.ZERO : template.getPrice().setScale(2, RoundingMode.HALF_UP);
        }
        throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "订单类型不正确");
    }

    private Coupon resolveCoupon(Long userId, String couponCode, BigDecimal amount) {
        if (!StringUtils.hasText(couponCode)) {
            return null;
        }
        Coupon coupon = couponService.claim(userId, couponCode);
        if (coupon.getMinAmount() != null && amount.compareTo(coupon.getMinAmount()) < 0) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "订单金额未达到优惠券使用门槛");
        }
        return coupon;
    }

    private BigDecimal calculateDiscount(BigDecimal amount, Coupon coupon) {
        if (coupon == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if ("RATE".equals(coupon.getCouponType())) {
            return amount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountRate())).setScale(2, RoundingMode.HALF_UP);
        }
        return coupon.getDiscountAmount() == null ? BigDecimal.ZERO : coupon.getDiscountAmount().min(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private void markCouponUsed(PayOrder order) {
        if (order.getCouponId() == null) {
            return;
        }
        Coupon coupon = couponService.getById(order.getCouponId());
        if (coupon == null || !"UNUSED".equals(coupon.getStatus())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "优惠券不可用");
        }
        coupon.setStatus("USED");
        coupon.setUsedTime(LocalDateTime.now());
        couponService.updateById(coupon);
    }

    private void releaseCoupon(PayOrder order) {
        if (order.getCouponId() == null) {
            return;
        }
        Coupon coupon = couponService.getById(order.getCouponId());
        if (coupon == null || !"USED".equals(coupon.getStatus())) {
            return;
        }
        // 关闭未支付订单时释放误占用的优惠券，避免用户后续无法再次使用。
        coupon.setStatus("UNUSED");
        coupon.setUsedTime(null);
        couponService.updateById(coupon);
    }

    private PayOrder getOwnedOrder(Long userId, Long orderId) {
        PayOrder order = getRequiredOrder(orderId);
        if (!userId.equals(order.getUserId())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "订单不存在");
        }
        return order;
    }

    private PayOrder getRequiredOrder(Long orderId) {
        PayOrder order = this.getById(orderId);
        if (order == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "订单不存在");
        }
        return order;
    }

    private void grantTemplate(Long userId, Long templateId) {
        UserTemplate owned = userTemplateService.getOne(new LambdaQueryWrapper<UserTemplate>()
                .eq(UserTemplate::getUserId, userId)
                .eq(UserTemplate::getTemplateId, templateId)
                .last("LIMIT 1"));
        if (owned != null) {
            return;
        }
        userTemplateService.save(UserTemplate.builder()
                .userId(userId)
                .templateId(templateId)
                .source("PAID")
                .active(0)
                .build());
    }

    private String appendRemark(String oldRemark, Long operatorId, String action, String text) {
        String content = StringUtils.hasText(text) ? text.trim() : "-";
        String line = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] "
                + action + (operatorId == null ? "" : " 操作人：" + operatorId) + "，原因：" + content;
        return StringUtils.hasText(oldRemark) ? oldRemark + "\n" + line : line;
    }

    private String generateOrderNo() {
        return "BGO" + System.currentTimeMillis() + (int) (Math.random() * 9000 + 1000);
    }

    private Optional<LocalDateTime> parseTime(String value, boolean endOfDay) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        String text = value.trim();
        try {
            if (text.length() == 10) {
                String suffix = endOfDay ? " 23:59:59" : " 00:00:00";
                return Optional.of(LocalDateTime.parse(text + suffix, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            return Optional.of(LocalDateTime.parse(text.replace("T", " "), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (Exception ignored) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "时间格式不正确");
        }
    }
}
