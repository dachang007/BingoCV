package net.de5.yeoh.bingocv.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import net.de5.yeoh.bingocv.common.api.CurrentUserInfo;
import net.de5.yeoh.bingocv.common.api.Result;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.utils.mvc.RequestUtils;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.model.domain.OperationLog;
import net.de5.yeoh.bingocv.service.OperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Around("execution(* net.de5.yeoh.bingocv.controller..*(..))")
    public Object record(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Integer status = 1;
        String errorMsg = null;
        try {
            Object result = joinPoint.proceed();
            if (result instanceof Result<?> apiResult && apiResult.getCode() != InfoEnum.SUCCESS.getCode()) {
                status = 0;
                errorMsg = apiResult.getMsg();
            }
            return result;
        } catch (Throwable e) {
            status = 0;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            saveLog(joinPoint, System.currentTimeMillis() - start, status, errorMsg);
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long costMs, Integer status, String errorMsg) {
        HttpServletRequest request = RequestUtils.getRequest();
        if (request == null || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        CurrentUserInfo user = UserContext.currentUser();
        Method method = resolveMethod(joinPoint);
        String module = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String action = method == null ? joinPoint.getSignature().getName() : method.getName();

        // 日志写入失败不能影响主业务，避免日志表异常拖垮正常接口。
        try {
            operationLogService.save(OperationLog.builder()
                    .userId(user == null ? null : user.getId())
                    .username(user == null ? null : user.getUsername())
                    .module(module)
                    .action(action)
                    .requestMethod(request.getMethod())
                    .requestUri(request.getRequestURI())
                    .ip(clientIp(request))
                    .status(status)
                    .errorMsg(errorMsg)
                    .costMs(costMs)
                    .build());
        } catch (Exception ignored) {
        }
    }

    private Method resolveMethod(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Method[] methods = joinPoint.getSignature().getDeclaringType().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
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
