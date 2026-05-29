package net.de5.yeoh.bingocv.service;

import net.de5.yeoh.bingocv.model.domain.Info;

/**
 * 个人简历Service
 * @author: daChang
 * @date: 2026/5/11 20:56
 */
public interface InfoService {

    /**
     * 统一提交个人简历（在一个事务中保存用户+教育+工作+技能+特长）
     * @param info 简历数据
     * @param userId 当前登录用户ID
     * @return 用户id
     */
    Long submit(Info info, Long userId);

    /**
     * 根据用户id获取个人简历（聚合各子表信息）
     * @param userId 用户id
     * @return 个人简历
     */
    Info getInfoByUserId(Long userId);
}
