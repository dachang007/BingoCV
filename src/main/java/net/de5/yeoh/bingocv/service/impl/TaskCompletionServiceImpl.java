package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.de5.yeoh.bingocv.mapper.UserTaskMapper;
import net.de5.yeoh.bingocv.model.domain.Task;
import net.de5.yeoh.bingocv.model.domain.UserTask;
import net.de5.yeoh.bingocv.service.TaskCompletionService;
import net.de5.yeoh.bingocv.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 抽取任务完成度公共类，避免循环依赖
 */
@Service
public class TaskCompletionServiceImpl implements TaskCompletionService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserTaskMapper userTaskMapper;

    /**
     * 任务完成度更新
     * @param userId 用户ID
     * @param taskKey 任务唯一标识
     */
    @Override
    public void completeTask(Long userId, String taskKey) {
        Task task = taskService.getOne(new LambdaQueryWrapper<Task>()
                .eq(Task::getTaskKey, taskKey)
                .eq(Task::getEnabled, 1)
                .last("LIMIT 1"));
        if (task == null) {
            return;
        }

        UserTask userTask = userTaskMapper.selectOne(new LambdaQueryWrapper<UserTask>()
                .eq(UserTask::getUserId, userId)
                .eq(UserTask::getTaskId, task.getId())
                .last("LIMIT 1"));
        if (userTask == null) {
            // 首次达成任务时创建用户任务记录，奖励仍由领取动作单独发放。
            userTaskMapper.insert(UserTask.builder()
                    .userId(userId)
                    .taskId(task.getId())
                    .progress(100)
                    .completed(1)
                    .rewarded(0)
                    .completeTime(LocalDateTime.now())
                    .build());
            return;
        }

        if (!Integer.valueOf(1).equals(userTask.getCompleted())
                || userTask.getProgress() == null
                || userTask.getProgress() < 100) {
            userTask.setCompleted(1);
            userTask.setProgress(100);
            userTask.setCompleteTime(LocalDateTime.now());
            userTaskMapper.updateById(userTask);
        }
    }
}
