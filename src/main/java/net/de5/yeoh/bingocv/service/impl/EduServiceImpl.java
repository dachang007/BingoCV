package net.de5.yeoh.bingocv.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.model.domain.Edu;
import net.de5.yeoh.bingocv.service.EduService;
import net.de5.yeoh.bingocv.service.TaskCompletionService;
import net.de5.yeoh.bingocv.mapper.EduMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EduServiceImpl extends ServiceImpl<EduMapper, Edu>
    implements EduService{

    @Autowired
    private TaskCompletionService taskCompletionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Edu> add(List<Edu> eduList) {
        if (CollUtil.isEmpty(eduList)) {
            throw new InfoException(InfoEnum.EDU_PARAMS_ERROR.getCode(), "教育经历不能为空");
        }
        Long userId = eduList.get(0).getUserId();
        
        // 检查用户是否已有教育经历（首次添加时触发任务）
        boolean hasExistingEdu = this.count(new LambdaQueryWrapper<Edu>().eq(Edu::getUserId, userId)) > 0;
        
        this.remove(new LambdaQueryWrapper<Edu>().eq(Edu::getUserId, userId));
        for (int i = 0; i < eduList.size(); i++) {
            Edu edu = eduList.get(i);
            edu.setUserId(userId);
            edu.setPriority(eduList.size() - i);
            edu.setCreateTime(LocalDateTime.now());
            edu.setUpdateTime(LocalDateTime.now());
        }
        this.saveBatch(eduList);
        
        // 如果是首次添加教育经历，触发任务
        if (!hasExistingEdu && !eduList.isEmpty()) {
            taskCompletionService.completeTask(userId, "add_first_edu");
        }
        
        return eduList;
    }

    @Override
    public List<Edu> listByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Edu>()
                .eq(Edu::getUserId, userId)
                .orderByDesc(Edu::getPriority));
    }
}
