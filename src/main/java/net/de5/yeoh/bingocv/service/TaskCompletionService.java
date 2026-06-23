package net.de5.yeoh.bingocv.service;

/**
 * 任务完成状态服务。
 * <p>
 * 只负责把任务标记为完成，避免积分、简历、任务奖励等业务服务互相注入造成循环依赖。
 */
public interface TaskCompletionService {

    /**
     * 按任务标识标记用户任务完成。
     *
     * @param userId 用户ID
     * @param taskKey 任务唯一标识
     */
    void completeTask(Long userId, String taskKey);
}
