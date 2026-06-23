package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.mapper.UserTaskMapper;
import net.de5.yeoh.bingocv.model.domain.Task;
import net.de5.yeoh.bingocv.model.domain.UserTask;
import net.de5.yeoh.bingocv.service.PointsService;
import net.de5.yeoh.bingocv.service.TaskCompletionService;
import net.de5.yeoh.bingocv.service.TaskService;
import net.de5.yeoh.bingocv.service.UserTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserTaskServiceImpl extends ServiceImpl<UserTaskMapper, UserTask> implements UserTaskService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private PointsService pointsService;

    @Autowired
    private TaskCompletionService taskCompletionService;

    @Override
    public void completeTask(Long userId, String taskKey) {
        taskCompletionService.completeTask(userId, taskKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int claimReward(Long userId, Long taskId) {
        Task task = taskService.getById(taskId);
        if (task == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "任务不存在");
        }

        UserTask userTask = this.getOne(new LambdaQueryWrapper<UserTask>()
                .eq(UserTask::getUserId, userId)
                .eq(UserTask::getTaskId, taskId)
                .last("LIMIT 1"));

        if (userTask == null || !Integer.valueOf(1).equals(userTask.getCompleted())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "任务尚未完成");
        }

        if (Integer.valueOf(1).equals(userTask.getRewarded())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "奖励已领取");
        }

        userTask.setRewarded(1);
        this.updateById(userTask);

        pointsService.change(userId, task.getRewardPoints(), "TASK_REWARD", task.getId(),
                "完成任务：" + task.getName());

        return task.getRewardPoints();
    }
}
