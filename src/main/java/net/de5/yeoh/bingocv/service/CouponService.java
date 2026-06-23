package net.de5.yeoh.bingocv.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.de5.yeoh.bingocv.model.domain.Coupon;
import net.de5.yeoh.bingocv.model.dto.CouponCreateRequest;

import java.util.List;
import java.util.Map;

public interface CouponService extends IService<Coupon> {
    List<Coupon> createCoupons(CouponCreateRequest request);

    Coupon claim(Long userId, String couponCode);

    Map<String, Object> myCoupons(Long userId);
}
