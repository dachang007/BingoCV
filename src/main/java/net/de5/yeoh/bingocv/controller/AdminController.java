package net.de5.yeoh.bingocv.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.api.Result;
import net.de5.yeoh.bingocv.common.utils.AdminUtils;
import net.de5.yeoh.bingocv.model.domain.AiUsageLog;
import net.de5.yeoh.bingocv.model.domain.OperationLog;
import net.de5.yeoh.bingocv.model.domain.PointsLog;
import net.de5.yeoh.bingocv.model.domain.Share;
import net.de5.yeoh.bingocv.model.domain.ShareAccess;
import net.de5.yeoh.bingocv.model.domain.SystemConfig;
import net.de5.yeoh.bingocv.model.domain.User;
import net.de5.yeoh.bingocv.service.AiUsageLogService;
import net.de5.yeoh.bingocv.service.EduService;
import net.de5.yeoh.bingocv.service.OperationLogService;
import net.de5.yeoh.bingocv.service.PointsLogService;
import net.de5.yeoh.bingocv.service.ProfilesService;
import net.de5.yeoh.bingocv.service.ShareAccessService;
import net.de5.yeoh.bingocv.service.ShareService;
import net.de5.yeoh.bingocv.service.SystemConfigService;
import net.de5.yeoh.bingocv.service.TemplateService;
import net.de5.yeoh.bingocv.service.UserService;
import net.de5.yeoh.bingocv.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api")
@Tag(name = "后台管理接口")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private ProfilesService profilesService;
    @Autowired
    private EduService eduService;
    @Autowired
    private WorkService workService;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private PointsLogService pointsLogService;
    @Autowired
    private ShareService shareService;
    @Autowired
    private ShareAccessService shareAccessService;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AiUsageLogService aiUsageLogService;

    @GetMapping("/configs")
    @CheckLogin
    @Operation(summary = "系统配置列表")
    public Result<List<SystemConfig>> configs(@RequestParam(required = false) String keyword) {
        requireAdmin();
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<SystemConfig>()
                .orderByAsc(SystemConfig::getConfigKey);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(SystemConfig::getConfigKey, keyword)
                    .or()
                    .like(SystemConfig::getDescription, keyword));
        }
        return Result.ok(systemConfigService.list(wrapper));
    }

    @PutMapping("/configs/{id}")
    @CheckLogin
    @Operation(summary = "保存系统配置")
    public Result<SystemConfig> updateConfig(@PathVariable Long id, @RequestBody SystemConfig request) {
        requireAdmin();
        SystemConfig config = systemConfigService.getById(id);
        if (config == null) {
            return Result.fail(400, "配置不存在");
        }
        config.setConfigValue(request.getConfigValue());
        config.setDescription(request.getDescription());
        config.setEnabled(request.getEnabled());
        systemConfigService.updateById(config);
        return Result.ok(config);
    }

    @GetMapping("/operation-logs")
    @CheckLogin
    @Operation(summary = "操作日志列表")
    public Result<Map<String, Object>> operationLogs(@RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                                     @RequestParam(required = false) String keyword) {
        requireAdmin();
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(OperationLog::getUsername, keyword)
                    .or()
                    .like(OperationLog::getRequestUri, keyword)
                    .or()
                    .like(OperationLog::getModule, keyword));
        }
        return Result.ok(pageResult(operationLogService.page(new Page<>(pageNum, pageSize), wrapper), pageNum, pageSize));
    }

    @GetMapping("/ai-usage-logs")
    @CheckLogin
    @Operation(summary = "AI 使用记录")
    public Result<Map<String, Object>> aiUsageLogs(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   @RequestParam(required = false) String actionType) {
        requireAdmin();
        LambdaQueryWrapper<AiUsageLog> wrapper = new LambdaQueryWrapper<AiUsageLog>()
                .orderByDesc(AiUsageLog::getCreateTime);
        if (StringUtils.hasText(actionType)) {
            wrapper.eq(AiUsageLog::getActionType, actionType);
        }
        return Result.ok(pageResult(aiUsageLogService.page(new Page<>(pageNum, pageSize), wrapper), pageNum, pageSize));
    }

    @GetMapping("/dashboard")
    @CheckLogin
    @Operation(summary = "数据看板")
    public Result<Map<String, Object>> dashboard() {
        requireAdmin();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        Map<String, Object> result = new HashMap<>();
        result.put("userCount", userService.count());
        result.put("profileCount", profilesService.count());
        result.put("educationCount", eduService.count());
        result.put("workCount", workService.count());
        result.put("templateCount", templateService.count());
        result.put("shareCount", shareService.count());
        result.put("todayShareAccessCount", shareAccessService.count(new LambdaQueryWrapper<ShareAccess>()
                .between(ShareAccess::getCreateTime, todayStart, todayEnd)));
        result.put("todayOperationCount", operationLogService.count(new LambdaQueryWrapper<OperationLog>()
                .between(OperationLog::getCreateTime, todayStart, todayEnd)));
        result.put("totalEarnedPoints", pointsLogService.list().stream()
                .filter(log -> log.getAmount() != null && log.getAmount() > 0)
                .mapToInt(PointsLog::getAmount)
                .sum());

        // 看板只取最近少量记录，避免进入页面时拉取过多明细数据。
        result.put("recentOperations", operationLogService.list(new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreateTime)
                .last("LIMIT 8")));
        result.put("recentShares", shareService.list(new LambdaQueryWrapper<Share>()
                .orderByDesc(Share::getCreateTime)
                .last("LIMIT 8")));
        result.put("trends", buildDashboardTrends());
        return Result.ok(result);
    }

    private Map<String, Object> buildDashboardTrends() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(13);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<String> dates = new ArrayList<>();
        List<Long> userGrowth = new ArrayList<>();
        List<Long> shareAccess = new ArrayList<>();
        List<Long> operationCount = new ArrayList<>();

        for (int i = 0; i < 14; i++) {
            LocalDate current = startDate.plusDays(i);
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = LocalDateTime.of(current, LocalTime.MAX);

            dates.add(current.format(formatter));
            // 按天聚合，前端直接渲染趋势图，避免页面侧重复计算。
            userGrowth.add(userService.count(new LambdaQueryWrapper<User>()
                    .between(User::getCreateTime, dayStart, dayEnd)));
            shareAccess.add(shareAccessService.count(new LambdaQueryWrapper<ShareAccess>()
                    .between(ShareAccess::getCreateTime, dayStart, dayEnd)));
            operationCount.add(operationLogService.count(new LambdaQueryWrapper<OperationLog>()
                    .between(OperationLog::getCreateTime, dayStart, dayEnd)));
        }

        Map<String, Object> trends = new HashMap<>();
        trends.put("dates", dates);
        trends.put("userGrowth", userGrowth);
        trends.put("shareAccess", shareAccess);
        trends.put("operationCount", operationCount);
        return trends;
    }

    private Map<String, Object> pageResult(Page<?> page, Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    private void requireAdmin() {
        AdminUtils.requireAdmin(userService);
    }
}
