package net.de5.yeoh.bingocv.common.utils.mvc;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * 响应工具类
 */
public class ResponseUtils {

    /**
     * 获取response
     *
     * @return HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes servletRequestAttributes = ServletRequestAttributesUtils.getServletRequestAttributes();
        return servletRequestAttributes == null ? null : servletRequestAttributes.getResponse();
    }

    /**
     * 设置响应header信息
     *
     * @param key   header key
     * @param value header 值
     */
    public static void setResponseHeader(String key, String value) {
        HttpServletResponse response = getResponse();
        if (response == null) {
            return;
        }
        response.setHeader(key, value);
    }
}
