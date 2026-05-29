package net.de5.yeoh.bingocv.common.aspect;

import lombok.extern.slf4j.Slf4j;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.enums.SystemEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 登录切面
 * @author: daChang
 * @date: 2026/5/11 17:22
 */
@Aspect
@Component
@Slf4j
public class LoginAspect {

    /**
     * 在执行有 @CheckLogin 注解的方法前执行
     */
    @Before("@annotation(checkLogin)")
    public void doBefore(CheckLogin checkLogin) {
        Long userId = UserContext.currentUserId();
        log.info("@CheckLogin 检查 - 当前用户ID: {}", userId);
        log.info("@CheckLogin 检查 - 完整用户信息: {}", UserContext.currentUser());
        
        if (userId == null) {
            log.warn("拦截到未登录请求 - UserContext 中没有用户信息");
            throw new InfoException(SystemEnum.USER_NOT_LOGIN_ERROR.getCode(), "该操作需要登录，请先登录");
        }
        
        log.info("@CheckLogin 检查通过 - 用户ID: {}", userId);
    }
}
