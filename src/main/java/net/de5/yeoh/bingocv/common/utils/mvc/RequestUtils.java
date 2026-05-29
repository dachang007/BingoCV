package net.de5.yeoh.bingocv.common.utils.mvc;

import cn.hutool.core.collection.CollUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

/**
 * 请求工具
 * @author: daChang
 * @date: 2026/5/10 15:51
 */
public class RequestUtils {
    /**
     * 获取当前线程请求对象
     *
     * @return 请求对象
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes servletRequestAttributes = ServletRequestAttributesUtils.getServletRequestAttributes();
        return servletRequestAttributes == null ? null : servletRequestAttributes.getRequest();
    }

    /**
     * 从header头中获取一个header值
     *
     * @param headerKey header头
     * @return header值
     */
    public static String getValueFromHeader(String headerKey) {
        HttpServletRequest request = getRequest();
        return request == null ? null : request.getHeader(headerKey);
    }

    /**
     * 从header头像获取一个header的多个值列表
     * @param headerKey header头
     * @return header值列表
     */
    public static List<String> getValuesFromHeader(String headerKey) {
        HttpServletRequest request = getRequest();
        return request == null ? Collections.emptyList(): Collections.list(request.getHeaders(headerKey));
    }
}
