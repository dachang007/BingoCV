package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.mapper.PointsMapper;
import net.de5.yeoh.bingocv.model.domain.Points;
import net.de5.yeoh.bingocv.model.domain.PointsLog;
import net.de5.yeoh.bingocv.model.domain.SignIn;
import net.de5.yeoh.bingocv.model.domain.Task;
import net.de5.yeoh.bingocv.model.domain.UserTask;
import net.de5.yeoh.bingocv.service.PointsLogService;
import net.de5.yeoh.bingocv.service.PointsService;
import net.de5.yeoh.bingocv.service.SignInService;
import net.de5.yeoh.bingocv.service.TaskService;
import net.de5.yeoh.bingocv.service.UserTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PointsServiceImpl extends ServiceImpl<PointsMapper, Points> implements PointsService {

    @Autowired
    private PointsLogService pointsLogService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserTaskService userTaskService;
    @Autowired
    private SignInService signInService;

    @Override
    public Points getOrCreate(Long userId) {
        Points points = this.getOne(new LambdaQueryWrapper<Points>().eq(Points::getUserId, userId));
        if (points != null) {
            return points;
        }
        points = Points.builder()
                .userId(userId)
                .balance(0)
                .totalEarned(0)
                .totalSpent(0)
                .build();
        this.save(points);
        return points;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Points change(Long userId, int amount, String bizType, Long bizId, String remark) {
        Points points = getOrCreate(userId);
        int nextBalance = points.getBalance() + amount;
        if (nextBalance < 0) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "积分不足");
        }

        points.setBalance(nextBalance);
        if (amount > 0) {
            points.setTotalEarned(points.getTotalEarned() + amount);
        } else {
            points.setTotalSpent(points.getTotalSpent() + Math.abs(amount));
        }
        points.setUpdateTime(LocalDateTime.now());
        this.updateById(points);

        pointsLogService.save(PointsLog.builder()
                .userId(userId)
                .bizType(bizType)
                .bizId(bizId)
                .amount(amount)
                .balance(nextBalance)
                .remark(remark)
                .build());
        return points;
    }

    @Override
    public Map<String, Object> dashboard(Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("account", getOrCreate(userId));
        result.put("logs", logs(userId));
        result.put("tasks", tasks(userId));
        result.put("todaySigned", signInService.count(new LambdaQueryWrapper<SignIn>()
                .eq(SignIn::getUserId, userId)
                .eq(SignIn::getSignDate, LocalDate.now())) > 0);
        return result;
    }

    @Override
    public List<PointsLog> logs(Long userId) {
        return pointsLogService.list(new LambdaQueryWrapper<PointsLog>()
                .eq(PointsLog::getUserId, userId)
                .orderByDesc(PointsLog::getCreateTime)
                .last("LIMIT 20"));
    }

    @Override
    public List<Task> tasks(Long userId) {
        List<Task> tasks = taskService.list(new LambdaQueryWrapper<Task>()
                .eq(Task::getEnabled, 1)
                .orderByAsc(Task::getTaskType)
                .orderByAsc(Task::getId));
        Map<Long, UserTask> userTaskMap = userTaskService.list(new LambdaQueryWrapper<UserTask>()
                        .eq(UserTask::getUserId, userId))
                .stream()
                .collect(Collectors.toMap(UserTask::getTaskId, Function.identity(), (a, b) -> a));
        tasks.forEach(task -> {
            UserTask userTask = userTaskMap.get(task.getId());
            task.setCompleted(userTask != null && Integer.valueOf(1).equals(userTask.getCompleted()));
            task.setRewarded(userTask != null && Integer.valueOf(1).equals(userTask.getRewarded()));
        });
        return tasks;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignIn signIn(Long userId) {
        LocalDate today = LocalDate.now();
        SignIn exists = signInService.getOne(new LambdaQueryWrapper<SignIn>()
                .eq(SignIn::getUserId, userId)
                .eq(SignIn::getSignDate, today));
        if (exists != null) {
            return exists;
        }

        SignIn yesterday = signInService.getOne(new LambdaQueryWrapper<SignIn>()
                .eq(SignIn::getUserId, userId)
                .eq(SignIn::getSignDate, today.minusDays(1))
                .last("LIMIT 1"));
        int streak = yesterday == null ? 1 : yesterday.getStreakDays() + 1;
        int[] rewards = {5, 8, 12, 16, 20, 25, 30};
        int reward = rewards[Math.min(streak, rewards.length) - 1];

        SignIn signIn = SignIn.builder()
                .userId(userId)
                .signDate(today)
                .streakDays(streak)
                .rewardPoints(reward)
                .build();
        signInService.save(signIn);
        change(userId, reward, "SIGN_IN", signIn.getId(), "每日签到");
        return signIn;
    }
}
