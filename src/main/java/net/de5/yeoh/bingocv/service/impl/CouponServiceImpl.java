package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.mapper.CouponMapper;
import net.de5.yeoh.bingocv.model.domain.Coupon;
import net.de5.yeoh.bingocv.model.dto.CouponCreateRequest;
import net.de5.yeoh.bingocv.service.CouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private static final String STATUS_UNUSED = "UNUSED";
    private static final String STATUS_USED = "USED";
    private static final String STATUS_DISABLED = "DISABLED";
    private static final String TYPE_AMOUNT = "AMOUNT";
    private static final String TYPE_RATE = "RATE";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Coupon> createCoupons(CouponCreateRequest request) {
        validateCreateRequest(request);
        int quantity = request.getQuantity() == null ? 1 : Math.max(1, Math.min(request.getQuantity(), 200));
        List<Coupon> coupons = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            coupons.add(Coupon.builder()
                    .couponCode(generateCode())
                    .name(request.getName().trim())
                    .couponType(request.getCouponType().toUpperCase())
                    .discountAmount(request.getDiscountAmount())
                    .discountRate(request.getDiscountRate())
                    .minAmount(defaultZero(request.getMinAmount()))
                    .targetUserId(request.getTargetUserId())
                    .status(STATUS_UNUSED)
                    .expireTime(request.getExpireTime())
                    .remark(request.getRemark())
                    .build());
        }
        this.saveBatch(coupons);
        return coupons;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Coupon claim(Long userId, String couponCode) {
        if (!StringUtils.hasText(couponCode)) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "请输入优惠券码");
        }
        Coupon coupon = this.getOne(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getCouponCode, couponCode.trim().toUpperCase())
                .last("LIMIT 1"));
        if (coupon == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "优惠券不存在");
        }
        if (!STATUS_UNUSED.equals(coupon.getStatus())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "优惠券不可领取或已被使用");
        }
        if (isExpired(coupon)) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "优惠券已过期");
        }
        if (coupon.getTargetUserId() != null && !coupon.getTargetUserId().equals(userId)) {
            throw new InfoException(InfoEnum.NO_AUTH_ERROR.getCode(), "该优惠券不属于当前用户");
        }
        if (coupon.getTargetUserId() == null) {
            coupon.setTargetUserId(userId);
            this.updateById(coupon);
        }
        return coupon;
    }

    @Override
    public Map<String, Object> myCoupons(Long userId) {
        List<Coupon> coupons = this.list(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getTargetUserId, userId)
                .orderByDesc(Coupon::getCreateTime));
        Map<String, Object> result = new HashMap<>();
        result.put("unused", coupons.stream().filter(coupon -> STATUS_UNUSED.equals(coupon.getStatus()) && !isExpired(coupon)).toList());
        result.put("used", coupons.stream().filter(coupon -> STATUS_USED.equals(coupon.getStatus())).toList());
        result.put("expired", coupons.stream().filter(this::isExpired).toList());
        result.put("disabled", coupons.stream().filter(coupon -> STATUS_DISABLED.equals(coupon.getStatus())).toList());
        result.put("all", coupons);
        return result;
    }

    private void validateCreateRequest(CouponCreateRequest request) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "请输入优惠券名称");
        }
        String type = StringUtils.hasText(request.getCouponType()) ? request.getCouponType().toUpperCase() : "";
        if (!TYPE_AMOUNT.equals(type) && !TYPE_RATE.equals(type)) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "优惠券类型不正确");
        }
        if (TYPE_AMOUNT.equals(type) && (request.getDiscountAmount() == null || request.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "请输入有效的优惠金额");
        }
        if (TYPE_RATE.equals(type) && (request.getDiscountRate() == null
                || request.getDiscountRate().compareTo(BigDecimal.ZERO) <= 0
                || request.getDiscountRate().compareTo(BigDecimal.ONE) >= 0)) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "折扣比例必须大于 0 且小于 1");
        }
    }

    private String generateCode() {
        String code;
        do {
            // BINGO 前缀方便用户识别，后半段随机码降低人工猜测概率。
            code = "BINGO" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (this.count(new LambdaQueryWrapper<Coupon>().eq(Coupon::getCouponCode, code)) > 0);
        return code;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isExpired(Coupon coupon) {
        return coupon.getExpireTime() != null && coupon.getExpireTime().isBefore(LocalDateTime.now());
    }
}
