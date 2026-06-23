package net.de5.yeoh.bingocv.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.model.domain.Work;
import net.de5.yeoh.bingocv.service.WorkService;
import net.de5.yeoh.bingocv.service.TaskCompletionService;
import net.de5.yeoh.bingocv.mapper.WorkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkServiceImpl extends ServiceImpl<WorkMapper, Work>
    implements WorkService{

    @Autowired
    private TaskCompletionService taskCompletionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Work> add(List<Work> workList) {
        if (CollUtil.isEmpty(workList)) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "工作经历不能为空");
        }
        Long userId = workList.get(0).getUserId();
        
        // 检查用户是否已有工作经历（首次添加时触发任务）
        boolean hasExistingWork = this.count(new LambdaQueryWrapper<Work>().eq(Work::getUserId, userId)) > 0;
        
        this.remove(new LambdaQueryWrapper<Work>().eq(Work::getUserId, userId));
        for (int i = 0; i < workList.size(); i++) {
            Work work = workList.get(i);
            work.setUserId(userId);
            work.setPriority(workList.size() - i);
            work.setCreateTime(LocalDateTime.now());
            work.setUpdateTime(LocalDateTime.now());
        }
        this.saveBatch(workList);
        
        // 如果是首次添加工作经历，触发任务
        if (!hasExistingWork && !workList.isEmpty()) {
            taskCompletionService.completeTask(userId, "add_first_work");
        }
        
        return workList;
    }

    @Override
    public List<Work> listByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Work>()
                .eq(Work::getUserId, userId)
                .orderByDesc(Work::getPriority));
    }
}
