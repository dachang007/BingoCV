package net.de5.yeoh.bingocv.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.api.Result;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.AdminUtils;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.model.domain.PayOrder;
import net.de5.yeoh.bingocv.model.domain.PayRefund;
import net.de5.yeoh.bingocv.model.dto.AdminOrderActionRequest;
import net.de5.yeoh.bingocv.model.dto.OrderCreateRequest;
import net.de5.yeoh.bingocv.model.vo.PaymentResultVO;
import net.de5.yeoh.bingocv.service.PayOrderService;
import net.de5.yeoh.bingocv.service.PayRefundService;
import net.de5.yeoh.bingocv.service.PaymentChannelService;
import net.de5.yeoh.bingocv.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/order")
@Tag(name = "支付订单接口")
public class OrderController {

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private PayRefundService payRefundService;
    @Autowired
    private PaymentChannelService paymentChannelService;
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    @CheckLogin
    @Operation(summary = "创建订单")
    public Result<PayOrder> create(@RequestBody OrderCreateRequest request) {
        return Result.ok(payOrderService.createOrder(requireUserId(), request));
    }

    @PostMapping("/{orderId}/pay")
    @CheckLogin
    @Operation(summary = "发起订单支付")
    public Result<PaymentResultVO> pay(@PathVariable Long orderId,
                                       @RequestParam(required = false) String payChannel) {
        return Result.ok(payOrderService.initiatePay(requireUserId(), orderId, payChannel));
    }

    @PostMapping("/callback/{payChannel}")
    @Operation(summary = "支付回调占位")
    public Result<PaymentResultVO> callback(@PathVariable String payChannel,
                                            @RequestBody(required = false) Map<String, String> payload) {
        return Result.ok(paymentChannelService.handleCallback(payChannel, payload));
    }

    @PostMapping("/{orderId}/close")
    @CheckLogin
    @Operation(summary = "关闭订单")
    public Result<PayOrder> close(@PathVariable Long orderId) {
        return Result.ok(payOrderService.close(requireUserId(), orderId));
    }

    @GetMapping("/{orderId}")
    @CheckLogin
    @Operation(summary = "订单详情")
    public Result<PayOrder> detail(@PathVariable Long orderId) {
        return Result.ok(payOrderService.detail(requireUserId(), orderId));
    }

    @GetMapping("/mine")
    @CheckLogin
    @Operation(summary = "我的订单")
    public Result<Map<String, Object>> mine(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(payOrderService.myOrders(requireUserId(), pageNum, pageSize));
    }

    @GetMapping("/admin/list")
    @CheckLogin
    @Operation(summary = "管理员订单列表")
    public Result<Map<String, Object>> adminList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String orderType,
                                                 @RequestParam(required = false) Long userId,
                                                 @RequestParam(required = false) String payChannel,
                                                 @RequestParam(required = false) String startTime,
                                                 @RequestParam(required = false) String endTime) {
        requireAdmin();
        return Result.ok(payOrderService.adminOrders(pageNum, pageSize, keyword, status, orderType, userId, payChannel, startTime, endTime));
    }

    @GetMapping("/admin/refunds")
    @CheckLogin
    @Operation(summary = "管理员退款记录列表")
    public Result<Map<String, Object>> adminRefunds(@RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(required = false) Long userId,
                                                    @RequestParam(required = false) String startTime,
                                                    @RequestParam(required = false) String endTime) {
        requireAdmin();
        LambdaQueryWrapper<PayRefund> wrapper = new LambdaQueryWrapper<PayRefund>()
                .orderByDesc(PayRefund::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(query -> query.like(PayRefund::getOrderNo, keyword)
                    .or()
                    .like(PayRefund::getRefundNo, keyword)
                    .or()
                    .like(PayRefund::getReason, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PayRefund::getRefundStatus, status.toUpperCase());
        }
        if (userId != null) {
            wrapper.eq(PayRefund::getUserId, userId);
        }
        parseTime(startTime, false).ifPresent(time -> wrapper.ge(PayRefund::getCreateTime, time));
        parseTime(endTime, true).ifPresent(time -> wrapper.le(PayRefund::getCreateTime, time));

        Page<PayRefund> page = payRefundService.page(new Page<>(pageNum, pageSize), wrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.ok(result);
    }

    @PostMapping("/admin/{orderId}/close")
    @CheckLogin
    @Operation(summary = "管理员关闭订单")
    public Result<PayOrder> adminClose(@PathVariable Long orderId,
                                       @RequestBody(required = false) AdminOrderActionRequest request) {
        requireAdmin();
        return Result.ok(payOrderService.adminClose(requireUserId(), orderId, request == null ? null : request.getReason()));
    }

    @PostMapping("/admin/{orderId}/refund")
    @CheckLogin
    @Operation(summary = "管理员退款")
    public Result<PayOrder> adminRefund(@PathVariable Long orderId,
                                        @RequestBody(required = false) AdminOrderActionRequest request) {
        requireAdmin();
        return Result.ok(payOrderService.adminRefund(requireUserId(), orderId,
                request == null ? null : request.getReason(),
                request == null ? null : request.getRefundAmount()));
    }

    @PutMapping("/admin/{orderId}/remark")
    @CheckLogin
    @Operation(summary = "管理员更新订单备注")
    public Result<PayOrder> adminRemark(@PathVariable Long orderId,
                                        @RequestBody(required = false) AdminOrderActionRequest request) {
        requireAdmin();
        return Result.ok(payOrderService.updateAdminRemark(requireUserId(), orderId, request == null ? null : request.getRemark()));
    }

    private Long requireUserId() {
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new InfoException(InfoEnum.NOT_LOGIN);
        }
        return userId;
    }

    private void requireAdmin() {
        AdminUtils.requireAdmin(userService);
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
