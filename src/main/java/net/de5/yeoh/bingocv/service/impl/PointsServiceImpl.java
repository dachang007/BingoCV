package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.mapper.PointsMapper;
import net.de5.yeoh.bingocv.mapper.UserTaskMapper;
import net.de5.yeoh.bingocv.model.domain.*;
import net.de5.yeoh.bingocv.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private UserTaskMapper userTaskMapper;
    @Autowired
    private TaskCompletionService taskCompletionService;
    @Autowired
    private SignInService signInService;
    @Autowired
    private ProfilesService profilesService;
    @Autowired
    private EduService eduService;
    @Autowired
    private WorkService workService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private SpecialtyService specialtyService;
    @Autowired
    private UserTemplateService userTemplateService;
    @Autowired
    private ShareService shareService;

    /**
     * 获取或创建用户积分记录
     * @param userId 用户ID
     * @return 用户积分记录
     */
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

    /**
     * 改变用户积分
     * @param userId 用户ID
     * @param amount 积分变化
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param remark 备注
     * @return 更新后的积分记录
     */
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

    /**
     * 获取用户积分中心数据
     * @param userId 用户ID
     * @return 用户积分中心数据
     */
    @Override
    public Map<String, Object> dashboard(Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("account", getOrCreate(userId));
        result.put("logs", logs(userId));
        result.put("tasks", tasks(userId));
        result.put("todaySigned", hasSignedToday(userId));
        return result;
    }

    /**
     * 获取用户积分日志
     * @param userId 用户ID
     * @return 用户积分日志
     */
    @Override
    public List<PointsLog> logs(Long userId) {
        return pointsLogService.list(new LambdaQueryWrapper<PointsLog>()
                .eq(PointsLog::getUserId, userId)
                .orderByDesc(PointsLog::getCreateTime)
                .last("LIMIT 20"));
    }

    /**
     * 获取用户积分任务
     * @param userId 用户ID
     * @return 用户积分任务
     */
    @Override
    public List<Task> tasks(Long userId) {
        Map<String, Integer> liveProgress = calculateTaskProgress(userId);

        // 每次打开积分中心都根据真实简历数据刷新任务完成状态。
        liveProgress.forEach((taskKey, progress) -> {
            if (progress >= 100) {
                taskCompletionService.completeTask(userId, taskKey);
            }
        });

        List<Task> tasks = taskService.list(new LambdaQueryWrapper<Task>()
                .eq(Task::getEnabled, 1)
                .orderByAsc(Task::getTaskType)
                .orderByAsc(Task::getId));
        Map<Long, UserTask> userTaskMap = userTaskMapper.selectList(new LambdaQueryWrapper<UserTask>()
                        .eq(UserTask::getUserId, userId))
                .stream()
                .collect(Collectors.toMap(UserTask::getTaskId, Function.identity(), (a, b) -> a));

        tasks.forEach(task -> {
            UserTask userTask = userTaskMap.get(task.getId());
            int progress = Math.max(liveProgress.getOrDefault(task.getTaskKey(), 0),
                    userTask == null || userTask.getProgress() == null ? 0 : userTask.getProgress());
            task.setProgress(Math.min(progress, 100));
            task.setCompleted(userTask != null && Integer.valueOf(1).equals(userTask.getCompleted()));
            task.setRewarded(userTask != null && Integer.valueOf(1).equals(userTask.getRewarded()));
        });
        return tasks;
    }

    /**
     * 用户签到
     * @param userId 用户ID
     * @return 签到记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignIn signIn(Long userId) {
        LocalDate today = LocalDate.now();
        SignIn exists = signInService.getOne(new LambdaQueryWrapper<SignIn>()
                .eq(SignIn::getUserId, userId)
                .eq(SignIn::getSignDate, today));
        if (exists != null) {
            taskCompletionService.completeTask(userId, "daily_login");
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
        taskCompletionService.completeTask(userId, "daily_login");
        return signIn;
    }

    /**
     * 计算用户积分任务进度
     * @param userId 用户ID
     * @return 用户积分任务进度
     */
    private Map<String, Integer> calculateTaskProgress(Long userId) {
        Map<String, Integer> progress = new HashMap<>();
        progress.put("daily_login", hasSignedToday(userId) ? 100 : 0);
        progress.put("complete_profile", profileProgress(userId));
        progress.put("add_first_edu", hasAnyEdu(userId) ? 100 : 0);
        progress.put("add_first_work", hasAnyWork(userId) ? 100 : 0);
        progress.put("select_template", hasActiveTemplate(userId) ? 100 : 0);
        progress.put("resume_full_score", resumeFullScore(userId));
        progress.put("share_resume", hasAnyShare(userId) ? 100 : 0);
        return progress;
    }

    /**
     * 检查用户是否已签到
     * @param userId 用户ID
     * @return 是否已签到
     */
    private boolean hasSignedToday(Long userId) {
        return signInService.count(new LambdaQueryWrapper<SignIn>()
                .eq(SignIn::getUserId, userId)
                .eq(SignIn::getSignDate, LocalDate.now())) > 0;
    }

    /**
     * 计算用户积分任务进度
     * @param userId 用户ID
     * @return 用户积分任务进度
     */
    private int profileProgress(Long userId) {
        Profiles profile = profilesService.getByUserId(userId);
        if (profile == null) {
            return 0;
        }
        int done = 0;
        done += StringUtils.hasText(profile.getName()) ? 1 : 0;
        done += StringUtils.hasText(profile.getPhone()) || StringUtils.hasText(profile.getEmail()) ? 1 : 0;
        done += StringUtils.hasText(profile.getCity()) ? 1 : 0;
        done += StringUtils.hasText(profile.getDescription()) ? 1 : 0;
        return done * 25;
    }

    /**
     * 计算用户积分任务进度
     * @param userId 用户ID
     * @return 用户积分任务进度
     */
    private int resumeFullScore(Long userId) {
        int score = 0;
        score += Math.round(profileProgress(userId) * 0.4f);
        score += hasAnyEdu(userId) ? 20 : 0;
        score += hasAnyWork(userId) ? 20 : 0;
        score += hasSkill(userId) ? 10 : 0;
        score += hasSpecialty(userId) ? 10 : 0;
        return Math.min(score, 100);
    }

    private boolean hasAnyEdu(Long userId) {
        return eduService.count(new LambdaQueryWrapper<Edu>().eq(Edu::getUserId, userId)) > 0;
    }

    private boolean hasAnyWork(Long userId) {
        return workService.count(new LambdaQueryWrapper<Work>().eq(Work::getUserId, userId)) > 0;
    }

    private boolean hasSkill(Long userId) {
        return skillService.count(new LambdaQueryWrapper<Skill>().eq(Skill::getUserId, userId)) > 0;
    }

    private boolean hasSpecialty(Long userId) {
        return specialtyService.count(new LambdaQueryWrapper<Specialty>().eq(Specialty::getUserId, userId)) > 0;
    }

    private boolean hasActiveTemplate(Long userId) {
        return userTemplateService.count(new LambdaQueryWrapper<UserTemplate>()
                .eq(UserTemplate::getUserId, userId)
                .eq(UserTemplate::getActive, 1)) > 0;
    }

    private boolean hasAnyShare(Long userId) {
        return shareService.count(new LambdaQueryWrapper<Share>().eq(Share::getUserId, userId)) > 0;
    }
}
