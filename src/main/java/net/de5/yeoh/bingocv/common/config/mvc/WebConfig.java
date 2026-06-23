package net.de5.yeoh.bingocv.common.config.mvc;

import net.de5.yeoh.bingocv.common.interceptor.RateLimitInterceptor;
import net.de5.yeoh.bingocv.common.interceptor.UserContextInterceptor;
import net.de5.yeoh.bingocv.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，用作上下文用户拦截生效
 * 
 * 功能说明：
 * - 配置用户信息拦截器，解析 USER-INFO 请求头
 * - 配置静态资源映射，支持上传文件访问
 * 
 * @author: daChang
 * @date: 2026/5/10 18:16
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 添加拦截器,通用的ControllerAdvice异常处理器
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(systemConfigService))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.ico",
                        "/uploads/**"
                );

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
                        "/user/register",
                        "/uploads/**"
                );
    }

    /**
     * 配置静态资源映射
     * 
     * 功能说明：
     * - 将 /uploads/** 路径映射到实际的文件存储目录
     * - 支持上传文件的 HTTP 访问
     * 
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadPath.replace("./", "");
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + location);
    }
}
