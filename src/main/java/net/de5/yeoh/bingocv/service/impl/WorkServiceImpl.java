package net.de5.yeoh.bingocv.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.model.domain.Work;
import net.de5.yeoh.bingocv.service.WorkService;
import net.de5.yeoh.bingocv.mapper.WorkMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author yeoh
* @description 针对表【bingo_work(用于描述用户的工作经历)】的数据库操作Service实现
* @createDate 2026-05-10 20:11:56
*/
@Service
public class WorkServiceImpl extends ServiceImpl<WorkMapper, Work>
    implements WorkService{

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Work> add(List<Work> workList) {
        if (CollUtil.isEmpty(workList)) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "工作经历不能为空");
        }
        Long userId = workList.get(0).getUserId();
        // 先删除该用户所有工作经历
        this.remove(new LambdaQueryWrapper<Work>().eq(Work::getUserId, userId));
        for (int i = 0; i < workList.size(); i++) {
            Work work = workList.get(i);
            work.setUserId(userId);
            work.setPriority(workList.size() - i);
            work.setCreateTime(LocalDateTime.now());
            work.setUpdateTime(LocalDateTime.now());
        }
        this.saveBatch(workList);
        return workList;
    }
}
