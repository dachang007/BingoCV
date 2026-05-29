package net.de5.yeoh.bingocv.common.utils.mvc;

import net.de5.yeoh.bingocv.common.api.CurrentUserInfo;

/**
 * 用户信息上下文，主要存储用户id
 */
public final class UserContext {

    /**
     * 私有构造函数，防止实例化
     */
    private UserContext(){}

    /**
     * 当前用户信息
     */
    private static final ThreadLocal<CurrentUserInfo> THREAD_LOCAL_USER = new ThreadLocal<>();

    /**
     * 获取当前用户id
     *
     * @return 用户id
     */
    public static Long currentUserId() {
        CurrentUserInfo userInfo = THREAD_LOCAL_USER.get();
        return userInfo == null ? null : userInfo.getId();
    }

    public static CurrentUserInfo currentUser() {
        return THREAD_LOCAL_USER.get();
    }

    /**
     * 设置当前用户id
     *
     * @param currentUserInfo 当前用户信息
     */
    public static void set(CurrentUserInfo currentUserInfo) {
        THREAD_LOCAL_USER.set(currentUserInfo);
    }

    /**
     * 清理当前线程中的用户信息
     */
    public static void clear(){
        THREAD_LOCAL_USER.remove();
    }
}
