package net.de5.yeoh.bingocv.common.config.mvc;

import net.de5.yeoh.bingocv.common.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，用作上下文用户拦截生效
 * @author: daChang
 * @date: 2026/5/10 18:16
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 添加拦截器,通用的ControllerAdvice异常处理器
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户信息拦截器 - 解析 USER-INFO 请求头并设置到 UserContext
        // 不再强制验证登录，由 @CheckLogin 注解来控制哪些接口需要登录
        registry.addInterceptor(new UserContextInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/",
                        "/u.html",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.ico",
                        "/layer/**",
                        "/u_files/**",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/captcha/**",
                        "/user/login",
                        "/user/register"
                );
    }
}
