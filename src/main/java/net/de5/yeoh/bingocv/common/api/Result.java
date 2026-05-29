package net.de5.yeoh.bingocv.common.api;

import cn.hutool.http.HttpStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;

import java.io.Serializable;

/**
 * 统一响应结果集
 * @author: daChang
 * @date: 2026/5/10 14:57
 */
@Data
public class Result<T> implements Serializable {

    @Schema(description = "业务状态码，200-成功，其它-失败")
    private int code;

    @Schema(description = "响应消息", example = "ok")
    private String msg;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "请求唯一标识", example = "1af123c11412e")
    private String requestId;

    // --- 构造方法 ---

    public Result() {}

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // --- 成功静态方法 ---

    public static <T> Result<T> ok() {
        return new Result<>(InfoEnum.SUCCESS.getCode(), InfoEnum.SUCCESS.getMsg(), null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(InfoEnum.SUCCESS.getCode(), InfoEnum.SUCCESS.getMsg(), data);
    }

    // --- 失败静态方法 ---

    /**
     * 最常用的失败方法：直接传入枚举
     */
    public static <T> Result<T> fail(InfoEnum infoEnum) {
        return new Result<>(infoEnum.getCode(), infoEnum.getMsg(), null);
    }

    /**
     * 允许自定义消息（比如枚举是参数错误，这里具体写“手机号格式不对”）
     */
    public static <T> Result<T> fail(InfoEnum infoEnum, String customMsg) {
        return new Result<>(infoEnum.getCode(), customMsg, null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    // 如果需要 requestId，可以加一个统一的 set 方法或者在拦截器里处理
}
