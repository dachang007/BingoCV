package net.de5.yeoh.bingocv.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.de5.yeoh.bingocv.common.constants.ErrorInfo;

/**
 * @author: daChang
 * @date: 2026/5/11 14:19
 */
@Getter
@AllArgsConstructor
public enum SystemEnum {
    USER_NOT_LOGIN_ERROR(ErrorInfo.Code.NOT_LOGIN,ErrorInfo.Msg.USER_NOT_LOGIN),
    PERMISSION_DENIED_ERROR(403, "权限不足"),
    RESOURCE_NOT_FOUND_ERROR(404, "资源未找到"),
    INTERNAL_SERVER_ERROR(500, "服务器错误");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态码对应的提示信息
     */
    private final String message;
}
