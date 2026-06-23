package net.de5.yeoh.bingocv.common.utils;

import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.model.domain.User;
import net.de5.yeoh.bingocv.service.UserService;

public final class AdminUtils {
    private AdminUtils() {
    }

    public static void requireAdmin(UserService userService) {
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new InfoException(InfoEnum.NOT_LOGIN);
        }
        User user = userService.getById(userId);
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new InfoException(InfoEnum.NO_AUTH_ERROR.getCode(), "仅管理员可操作");
        }
    }
}
