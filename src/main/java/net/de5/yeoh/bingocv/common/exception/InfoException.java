package net.de5.yeoh.bingocv.common.exception;

import lombok.Getter;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;

/**
 * 信息异常
 * @author: daChang
 * @date: 2026/5/11 12:26
 */
@Getter
public class InfoException extends RuntimeException{
    private final int code;

    /**
     * 支持通过错误码枚举创建异常
     */
    public InfoException(InfoEnum infoEnum, String message, int code) {
        super(message);
        this.code = code;
    }

    /**
     * 支持通过错误码枚举创建异常
     */
    public InfoException(InfoEnum infoEnum) {
        super(infoEnum.getMsg());
        this.code = infoEnum.getCode();
    }

    /**
     * 支持自定义消息（比如：该账号已在异地登录）
     */
    public InfoException(int code, String message) {
        super(message);
        this.code = code;
    }

    public InfoException(InfoEnum infoEnum, String message) {
        super(message);
        this.code = infoEnum.getCode();
    }
}
