package net.de5.yeoh.bingocv.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 个人简历信息错误枚举类
 * @author: daChang
 * @date: 2026/5/11 12:28
 */
@Getter
public enum InfoEnum {

    /**
     *
     */
    SUCCESS(200, "操作成功"),
    /**
     * 教育信息参数错误
     */
    EDU_PARAMS_ERROR(400, "教育信息参数错误"),
    /**
     * 请求参数错误
     */
    PARAMS_ERROR(400, "请求参数错误"),
    /**
     * 无权限错误
     */
    NO_AUTH_ERROR(403, "无权限"),
    /**
     * 禁止访问错误
     */
    FORBIDDEN_ERROR(403, "禁止访问"),
    /**
     * 系统内部异常
     */
    SYSTEM_ERROR(500, "系统内部异常"),
    /**
     * 操作失败错误
     */
    OPERATION_ERROR(50001, "操作失败"),

    /**
     * 用户名已存在
     */
    USERNAME_EXISTS(40001, "用户名已存在"),

    /**
     * 用户名或密码错误
     */
    LOGIN_ERROR(40002, "用户名或密码错误"),

    /**
     * 请先登录
     */
    NOT_LOGIN(40003, "请先登录"),

    /**
     * 账号已禁用
     */
    ACCOUNT_DISABLED(40004, "账号已禁用"),

    /**
     * 验证码错误
     */
    CAPTCHA_ERROR(40005, "验证码错误，请重新输入"),

    /**
     * 密码不一致
     */
    PASSWORD_NOT_MATCH(40006, "两次输入的密码不一致"),

    /**
     * 用户不存在
     */
    USERNAME_NOT_EXISTS(40007, "用户名已存在"),

    /**
     * 用户名或密码不正确
     */
    USERNAME_OR_PASS_IS_WRONG(40008, "用户名或密码不正确"),

    /**
     * 请先输入账号密码
     */
    USERNAME_AND_PASS_IS_EMPTY(40009, "请先输入账号密码"),

    /**
     * 用户不存在
     */
    USER_NOT_EXISTS(40010, "用户不存在"),

    /**
     * 个人信息不存在
     */
    PROFILE_NOT_EXISTS(40011, "个人信息不存在"),

    /**
     * 参数为空
     */
    PARAM_IS_EMPTY(40012, "参数不能为空"),

    /**
     * 账号或密码错误
     */
    USERNAME_OR_PASSWORD_ERROR(40013, "账号或密码错误");


    /*
     * 状态码
     */
    private final int code;

    /**
     * 错误提示信息
     */
    private final String msg;

    InfoEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
