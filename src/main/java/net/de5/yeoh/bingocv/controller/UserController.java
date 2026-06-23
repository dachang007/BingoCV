package net.de5.yeoh.bingocv.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.api.CurrentUserInfo;
import net.de5.yeoh.bingocv.common.api.Result;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.AdminUtils;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.model.domain.Profiles;
import net.de5.yeoh.bingocv.model.domain.User;
import net.de5.yeoh.bingocv.service.impl.CaptchaService;
import net.de5.yeoh.bingocv.service.ProfilesService;
import net.de5.yeoh.bingocv.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 账号接口
 * @author: daChang
 * @date: 2026/5/12 10:00
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "账号接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProfilesService profilesService;

    @Autowired
    private CaptchaService captchaService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> params) {
        log.info("收到注册请求, params: {}", params);

        String username = params.get("username");
        String password = params.get("password");
        String confirmPassword = params.get("confirmPassword");
        String captcha = params.get("captcha");
        String captchaId = params.get("captchaId");

        log.info("注册参数 - username: {}, password: [PROTECTED], confirmPassword: [PROTECTED], captcha: {}, captchaId: {}",
                username, captcha, captchaId);

        // 参数校验
        if (username == null || username.trim().isEmpty()) {
            log.warn("注册失败: 用户名为空");
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            log.warn("注册失败: 密码为空");
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "密码不能为空");
        }
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            log.warn("注册失败: 确认密码为空");
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "确认密码不能为空");
        }
        if (captcha == null || captcha.trim().isEmpty()) {
            log.warn("注册失败: 验证码为空");
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "验证码不能为空");
        }
        if (captchaId == null || captchaId.trim().isEmpty()) {
            log.warn("注册失败: 验证码ID为空");
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "验证码ID不能为空");
        }

        // 用户名长度校验
        if (username.trim().length() < 3 || username.trim().length() > 20) {
            log.warn("注册失败: 用户名长度不符合要求, length: {}", username.trim().length());
            throw new InfoException(InfoEnum.PARAMS_ERROR, "用户名长度必须在3-20个字符之间");
        }

        // 密码长度校验
        if (password.length() < 6 || password.length() > 20) {
            log.warn("注册失败: 密码长度不符合要求, length: {}", password.length());
            throw new InfoException(InfoEnum.PARAMS_ERROR, "密码长度必须在6-20个字符之间");
        }

        // 校验验证码（使用Redis）
        if (!captchaService.verifyCaptcha(captchaId, captcha)) {
            log.warn("注册失败: 验证码错误");
            throw new InfoException(InfoEnum.CAPTCHA_ERROR);
        }

        // 校验密码一致性
        if (!password.equals(confirmPassword)) {
            log.warn("注册失败: 两次密码不一致");
            throw new InfoException(InfoEnum.PASSWORD_NOT_MATCH);
        }

        // 注册账号
        log.info("开始注册用户: {}", username);
        User user = userService.register(username.trim(), password);
        log.info("用户注册成功, userId: {}", user.getUserid());

        // 创建默认个人信息
        Profiles profile = Profiles.builder()
                .userId(user.getUserid())
                .name(username.trim())
                .build();
        profilesService.create(profile);
        log.info("创建用户Profile成功, profileId: {}", profile.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("profile", profile);

        log.info("注册请求完成, 返回结果: code=200");
        return Result.ok(result);
    }

    /**
     * 用户登录
     * @param params 登录参数
     * @param request 请求对象
     * @return 登录结果
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params,
                                              HttpServletRequest request) {
        String username = params.get("username");
        String password = params.get("password");
        String captcha = params.get("captcha");
        String captchaId = params.get("captchaId");

        // 参数校验
        if (username == null || username.trim().isEmpty()) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "密码不能为空");
        }
        if (captcha == null || captcha.trim().isEmpty()) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "验证码不能为空");
        }
        if (captchaId == null || captchaId.trim().isEmpty()) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "验证码ID不能为空");
        }

        // 校验验证码
        if (!captchaService.verifyCaptcha(captchaId, captcha)) {
            throw new InfoException(InfoEnum.CAPTCHA_ERROR);
        }

        // 登录
        User user = userService.login(username.trim(), password);

        // 更新登录信息
        String ip = getClientIp(request);
        userService.updateLoginInfo(user.getUserid(), ip);

        // 获取个人信息
        Profiles profile = profilesService.getByUserId(user.getUserid());
        if (profile == null) {
            log.warn("用户 {} 登录成功但未找到个人信息", user.getUserid());
            // 创建默认个人信息
            profile = Profiles.builder()
                    .userId(user.getUserid())
                    .name(username.trim())
                    .build();
            profilesService.create(profile);
        }

        // 将用户信息存到ThreadLocal
        CurrentUserInfo userInfo = new CurrentUserInfo(
                user.getUserid(),
                user.getUsername(),
                user.getAvatar(),
                user.getRole(),
                null,
                user.getLastLoginIp()
        );
        UserContext.set(userInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("profile", profile);

        return Result.ok(result);
    }

    /**
     * 获取当前登录用户信息
     * @return 当前登录用户信息
     */
    @GetMapping("/info")
    @CheckLogin
    @Operation(summary = "获取当前登录用户信息")
    public Result<CurrentUserInfo> getCurrentUserInfo() {
        CurrentUserInfo userInfo = UserContext.currentUser();
        if (userInfo == null) {
            throw new InfoException(InfoEnum.NOT_LOGIN);
        }
        return Result.ok(userInfo);
    }

    /**
     * 获取用户列表（管理员）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param username 用户名搜索
     * @param status 状态筛选
     * @return 用户列表
     */
    @CheckLogin
    @GetMapping("/list")
    @Operation(summary = "获取用户列表")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        
        requireAdmin();
        
        Map<String, Object> result = userService.getUserList(pageNum, pageSize, username, status);
        return Result.ok(result);
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，第一个IP为真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 更新用户状态（管理员）
     * @param userId 用户ID
     * @param status 状态（0-正常，1-禁用）
     * @return 更新结果
     */
    @CheckLogin
    @PutMapping("/{userId}/status")
    @Operation(summary = "更新用户状态")
    public Result<User> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        
        requireAdmin();
        
        User user = userService.updateStatus(userId, status);
        return Result.ok(user);
    }

    private void requireAdmin() {
        AdminUtils.requireAdmin(userService);
    }
}
