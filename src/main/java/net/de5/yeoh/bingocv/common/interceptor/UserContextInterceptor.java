package net.de5.yeoh.bingocv.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.de5.yeoh.bingocv.common.api.CurrentUserInfo;
import net.de5.yeoh.bingocv.common.constants.HeaderConstants;
import net.de5.yeoh.bingocv.common.enums.SystemEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * 用户信息拦截器
 * @author: daChang
 * @date: 2026/5/10 17:37
 */
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {

    // 静态初始化对象映射器
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    // 需要跳过拦截的路径前缀列表
    private static final List<String> SKIP_PREFIXES = Arrays.asList(
            "/login/",
            "/css/",
            "/js/",
            "/img/",
            "/layer/",
            "/u_files/",
            "/webjars/",
            "/v3/api-docs/",
            "/captcha",
            "setting"
    );

    private static final List<String> SKIP_PATHS = Arrays.asList(
            "/",             // 根路径
            "/info",         // 简历查询接口（GET，允许公开访问）
            "/user/login",   // 登录接口
            "/user/register" // 注册接口
    );
    
    // 需要跳过拦截的文件扩展名列表
    private static final List<String> SKIP_EXTENSIONS = Arrays.asList(
            ".html",
            ".css",
            ".js",
            ".png",
            ".jpg",
            ".gif",
            ".svg",
            ".ico"
    );

    /**
     * 判断是否需要跳过拦截
     */
    private boolean shouldSkip(String requestUri) {
        // 检查完整路径匹配
        for (String path : SKIP_PATHS) {
            if (requestUri.equals(path)) {
                log.info("请求路径：{}，匹配完整路径：{}，跳过拦截", requestUri, path);
                return true;
            }
        }
        // 检查路径前缀
        for (String prefix : SKIP_PREFIXES) {
            if (requestUri.startsWith(prefix)) {
                log.info("请求路径：{}，匹配跳过前缀：{}，跳过拦截", requestUri, prefix);
                return true;
            }
        }
        // 检查文件扩展名
        for (String extension : SKIP_EXTENSIONS) {
            if (requestUri.endsWith(extension)) {
                log.info("请求路径：{}，匹配跳过扩展名：{}，跳过拦截", requestUri, extension);
                return true;
            }
        }
        log.info("请求路径：{}，不匹配跳过规则，将进行拦截处理", requestUri);
        return false;
    }

    /**
     * 在请求处理之前进行调用（Controller方法调用之前）
     * @param request    请求对象
     * @param response   响应对象
     * @param handler    处理对象
     * @return           是否继续处理请求
     * @throws Exception 异常对象
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("RequestURI: {}, Method: {}", request.getRequestURI(), request.getMethod());

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        // 如果有 context path，移除它以便匹配
        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            requestUri = requestUri.substring(contextPath.length());
        }
        
        // 跳过静态资源和公开页面
        if (shouldSkip(requestUri)) {
            return true;
        }

        // 1. 获取头信息
        String userInfo = request.getHeader(HeaderConstants.USER_INFO);
        log.info("请求路径：{}, USER-INFO 头信息：{}", requestUri, userInfo);

        // 2. 强制校验
        if (userInfo == null || userInfo.isEmpty()) {
            log.warn("请求路径：{}, 缺少 USER-INFO 头信息", requestUri);
            throw new InfoException(SystemEnum.USER_NOT_LOGIN_ERROR.getCode(), "您还未登录，请先登录！");
        }

        try {
            // 3. 解析逻辑（涵盖 Base64 和 JSON 解析）
            log.info("原始 Base64 字符串：{}", userInfo);
            byte[] decodedBytes = Base64.getDecoder().decode(userInfo);
            String jsonUserInfo = new String(decodedBytes, "UTF-8");
            log.info("解码后的 JSON: {}", jsonUserInfo);
            CurrentUserInfo currentUserInfo = OBJECT_MAPPER.readValue(jsonUserInfo, CurrentUserInfo.class);
            log.info("解析后的用户信息：userId={}, username={}", currentUserInfo.getId(), currentUserInfo.getUsername());

            // 4. 存入上下文
            UserContext.set(currentUserInfo);
            return true;
        } catch (Exception e) {
            // 这里建议捕获所有 Exception，因为解码或反序列化失败都属于"无效凭证"
            log.error("用户身份信息解析失败，值：{}，原因：{}", userInfo, e.getMessage(), e);
            throw new InfoException(SystemEnum.USER_NOT_LOGIN_ERROR.getCode(), "身份校验失败，请重新登录");
        }


    }

    /**
     * 在请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）
     * @param request    请求对象
     * @param response   响应对象
     * @param handler    处理对象
     * @param ex         异常对象
     * @throws Exception 异常对象
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}
