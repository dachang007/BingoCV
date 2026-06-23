package net.de5.yeoh.bingocv.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.service.SystemConfigService;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final SystemConfigService systemConfigService;
    private final Map<String, Deque<Long>> requestTimes = new ConcurrentHashMap<>();

    public RateLimitInterceptor(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        boolean enabled = systemConfigService.getBooleanValue("rate.limit.enabled", true);
        if (!enabled || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        int maxRequests = systemConfigService.getIntValue("rate.limit.max.requests", 120);
        int windowSeconds = systemConfigService.getIntValue("rate.limit.window.seconds", 60);
        long now = System.currentTimeMillis();
        long windowStart = now - windowSeconds * 1000L;
        String key = clientIp(request) + ":" + request.getRequestURI();

        // 滑动窗口限流：清理窗口外请求后，再判断当前接口是否超过阈值。
        Deque<Long> times = requestTimes.computeIfAbsent(key, ignored -> new ConcurrentLinkedDeque<>());
        while (!times.isEmpty() && times.peekFirst() < windowStart) {
            times.pollFirst();
        }
        if (times.size() >= maxRequests) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "请求过于频繁，请稍后再试");
        }
        times.addLast(now);
        return true;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp == null || realIp.isBlank() ? request.getRemoteAddr() : realIp;
    }
}
