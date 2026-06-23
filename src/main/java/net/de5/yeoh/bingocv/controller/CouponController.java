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
import net.de5.yeoh.bingocv.model.domain.Coupon;
import net.de5.yeoh.bingocv.model.dto.CouponCreateRequest;
import net.de5.yeoh.bingocv.service.CouponService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupon")
@Tag(name = "优惠券接口")
public class CouponController {

    @Autowired
    private CouponService couponService;
    @Autowired
    private UserService userService;

    @GetMapping("/mine")
    @CheckLogin
    @Operation(summary = "我的优惠券")
    public Result<Map<String, Object>> mine() {
        return Result.ok(couponService.myCoupons(requireUserId()));
    }

    @PostMapping("/claim")
    @CheckLogin
    @Operation(summary = "领取优惠券")
    public Result<Coupon> claim(@RequestBody Map<String, String> params) {
        return Result.ok(couponService.claim(requireUserId(), params.get("couponCode")));
    }

    @GetMapping("/admin/list")
    @CheckLogin
    @Operation(summary = "管理员优惠券列表")
    public Result<Map<String, Object>> adminList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String status) {
        requireAdmin();
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<Coupon>()
                .orderByDesc(Coupon::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(Coupon::getCouponCode, keyword)
                    .or()
                    .like(Coupon::getName, keyword)
                    .or()
                    .like(Coupon::getRemark, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Coupon::getStatus, status.toUpperCase());
        }

        Page<Coupon> page = couponService.page(new Page<>(pageNum, pageSize), wrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.ok(result);
    }

    @PostMapping("/admin/create")
    @CheckLogin
    @Operation(summary = "管理员创建优惠券")
    public Result<List<Coupon>> create(@RequestBody CouponCreateRequest request) {
        requireAdmin();
        return Result.ok(couponService.createCoupons(request));
    }

    @PutMapping("/admin/{id}/status")
    @CheckLogin
    @Operation(summary = "管理员更新优惠券状态")
    public Result<Coupon> updateStatus(@PathVariable Long id, @RequestParam String status) {
        requireAdmin();
        Coupon coupon = couponService.getById(id);
        if (coupon == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "优惠券不存在");
        }
        String nextStatus = status.toUpperCase();
        if (!"UNUSED".equals(nextStatus) && !"DISABLED".equals(nextStatus)) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "只允许启用或禁用未使用优惠券");
        }
        if ("USED".equals(coupon.getStatus())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "已使用优惠券不能修改状态");
        }
        coupon.setStatus(nextStatus);
        couponService.updateById(coupon);
        return Result.ok(coupon);
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
}
