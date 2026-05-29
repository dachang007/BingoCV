package net.de5.yeoh.bingocv.common.exception;

import lombok.Getter;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;

/**
 * 系统异常类，用于处理未登录、权限不足等系统级业务异常
 * @author: daChang
 * @date: 2026/5/11 14:17
 */
@Getter
public class SystemException extends RuntimeException{
    private final int code;

    /**
     * 支持通过错误码枚举创建异常
     */
    public SystemException(InfoEnum infoEnum) {
        super(infoEnum.getMsg());
        this.code = infoEnum.getCode();
    }

    /**
     * 支持自定义消息（比如：该账号已在异地登录）
     */
    public SystemException(InfoEnum infoEnum, String message) {
        super(message);
        this.code = infoEnum.getCode();
    }
}
