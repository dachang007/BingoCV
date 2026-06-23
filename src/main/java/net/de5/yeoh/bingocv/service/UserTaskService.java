package net.de5.yeoh.bingocv.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.de5.yeoh.bingocv.model.domain.UserTask;

public interface UserTaskService extends IService<UserTask> {

    /**
     * 标记任务完成
     * @param userId 用户ID
     * @param taskKey 任务标识
     */
    void completeTask(Long userId, String taskKey);

    /**
     * 领取任务奖励
     * @param userId 用户ID
     * @param taskId 任务ID
     * @return 积分变动数量
     */
    int claimReward(Long userId, Long taskId);
}
