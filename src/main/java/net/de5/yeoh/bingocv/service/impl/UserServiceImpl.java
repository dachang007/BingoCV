package net.de5.yeoh.bingocv.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.common.api.CurrentUserInfo;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.mapper.UserMapper;
import net.de5.yeoh.bingocv.model.domain.User;
import net.de5.yeoh.bingocv.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author yeoh
* @description 针对表【bingo_user(用户登录账户表)】的数据库操作Service实现
* @createDate 2026-05-12 14:40:47
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    /**
     * 注册
     * @param username 用户名
     * @param password 密码
     * @return  User
     */
    @Override
    public User register(String username, String password) {
        // 检查用户名是否已存在
        long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (count > 0) {
            throw new InfoException(InfoEnum.USERNAME_EXISTS);
        }
        
        // 创建账号
        User user = User.builder()
                .username(username)
                .password(BCrypt.hashpw(password))
                .status(0)
                .role("USER")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        this.save(user);
        
        // 返回时清除密码
        user.setPassword(null);
        return user;
    }

    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @return  User
     */
    @Override
    public User login(String username, String password) {
        // 查询用户
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        
        if (user == null) {
            throw new InfoException(InfoEnum.LOGIN_ERROR);
        }
        
        // 校验密码
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new InfoException(InfoEnum.LOGIN_ERROR);
        }
        
        // 检查账号状态 (0-正常, 1-禁用)
        if (user.getStatus() != 0) {
            throw new InfoException(InfoEnum.ACCOUNT_DISABLED);
        }
        
        // 返回时清除密码
        user.setPassword(null);
        
        // 将用户信息存到 ThreadLocal
        CurrentUserInfo userInfo = new CurrentUserInfo(
                user.getUserid(),
                user.getUsername(),
                user.getAvatar(),
                user.getRole(),
                Collections.singleton(user.getRole()),
                null
        );
        UserContext.set(userInfo);
        
        return user;
    }

    /**
     * 更新登录信息
     * @param userId 用户ID
     * @param loginIp 登录IP
     */
    @Override
    public void updateLoginInfo(Long userId, String loginIp) {
        User user = User.builder()
                .userid(userId)
                .lastLoginTime(LocalDateTime.now())
                .lastLoginIp(loginIp)
                .updateTime(LocalDateTime.now())
                .build();
        this.updateById(user);
    }

    /**
     * 获取用户列表
     * @param pageNum 页码
     * @param pageSize 页大小
     **/
    @Override
    public Map<String, Object> getUserList(Integer pageNum, Integer pageSize, String username, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 用户名模糊搜索
        if (username != null && !username.trim().isEmpty()) {
            queryWrapper.like(User::getUsername, username);
        }
        
        // 状态筛选
        if (status != null && status >= 0) {
            queryWrapper.eq(User::getStatus, status);
        }
        
        // 排除已删除的用户
        queryWrapper.eq(User::getDeleted, 0);
        
        // 按创建时间降序
        queryWrapper.orderByDesc(User::getCreateTime);
        
        // 分页查询
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> resultPage = this.page(page, queryWrapper);
        
        // 清除密码信息
        List<User> userList = resultPage.getRecords().stream()
                .map(user -> {
                    user.setPassword(null);
                    return user;
                })
                .collect(Collectors.toList());
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", userList);
        result.put("total", resultPage.getTotal());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        return result;
    }

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 状态（0-正常，1-禁用）
     * @return 更新后的用户信息
     */
    @Override
    public User updateStatus(Long userId, Integer status) {
        User user = this.getById(userId);
        if (user == null) {
            throw new InfoException(InfoEnum.USER_NOT_EXISTS);
        }
        
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        this.updateById(user);
        
        user.setPassword(null);
        return user;
    }
}
