package net.de5.yeoh.bingocv.common.annotation;

import java.lang.annotation.*;

/**
 * 检查是否登录注解
 * @author: daChang
 * @date: 2026/5/11 17:01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckLogin {
}