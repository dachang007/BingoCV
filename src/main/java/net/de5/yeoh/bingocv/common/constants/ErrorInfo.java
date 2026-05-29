package net.de5.yeoh.bingocv.common.constants;

import lombok.Getter;

/**
 * 错误信息常量类
 * @author: daChang
 * @date: 2026/5/11 14:21
 */
public class ErrorInfo {
    public static class Msg {
        /**
         * 失败
         */
        public static final String PROCESS_FAILD = "失败";

        /**
         * 用户未登录
         */
        public static final String USER_NOT_LOGIN = "用户未登录";
        /**
         * 请求异常默认异常信息
         */
        public static final String REQUEST_PARAM_ILLEGAL = "请求参数不合法";
        /**
         * 频繁请求
         */
        public static final String REQUEST_OPERATE_FREQUENTLY = "操作频繁,请稍后重试";
        /**
         * 请求超时
         */
        public static final String REQUEST_TIME_OUT = "请求超时";

        /**
         * 请求失败
         */
        public static final String REQUEST_FAILD = "请求失败";

        /**
         * 拒绝访问
         */
        public static final String REQUEST_FORBIDDEN = "拒绝访问";

        /**
         * 身份未识别
         */
        public static final String REQUEST_UNAUTHORIZED = "身份未识别";

        /**
         * 无访问权限
         */
        public static final String NO_PERMISSIONS = "无访问权限";

        /**
         * 禁止操作
         */
        public static final String FORBIDDEN_OPERATION = "禁止操作";
    }

    public static class Code {

        /**
         * 未登录
         */
        public static final int NOT_LOGIN = 600;

        /**
         * 登录过期
         */
        public static final int LOGIN_TIMEOUT = 601;

        /**
         * token不合法
         */
        public static final int ILLEGAL_TOKEN = 602;

        /**
         * 无权限访问
         */
        public static final int NO_PERMISSIONS = 603;

        /**
         * 禁止操作
         */
        public static final int FORBIDDEN_OPERATION = 604;

        /**
         * 账号冻结
         */
        public static final int ACCOUNT_FREEZED = 605;
    }
}

