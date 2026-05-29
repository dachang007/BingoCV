package net.de5.yeoh.bingocv.service;

import net.de5.yeoh.bingocv.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
* @author yeoh
* @description 针对表【bingo_user(用户登录账户表)】的数据库操作Service
* @createDate 2026-05-12 14:40:47
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @return 注册成功的账号信息（不含密码）
     */
    User register(String username, String password);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的账号信息（不含密码）
     */
    User login(String username, String password);

    /**
     * 更新登录信息
     * @param userId 用户ID
     * @param loginIp 登录IP
     */
    void updateLoginInfo(Long userId, String loginIp);

    /**
     * 获取用户列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param username 用户名搜索
     * @param status 状态筛选
     * @return 用户列表和总数
     */
    Map<String, Object> getUserList(Integer pageNum, Integer pageSize, String username, Integer status);
}