package net.de5.yeoh.bingocv.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.model.domain.Edu;
import net.de5.yeoh.bingocv.service.EduService;
import net.de5.yeoh.bingocv.mapper.EduMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author yeoh
* @description 针对表【bingo_edu(用于描述用户的学习)】的数据库操作Service实现
* @createDate 2026-05-10 20:11:56
*/
@Service
public class EduServiceImpl extends ServiceImpl<EduMapper, Edu>
    implements EduService{

    /**
     * 添加或更新用户教育经历
     * @param eduList 教育经历列表
     * @return 教育经历列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Edu> add(List<Edu> eduList) {
        if (CollUtil.isEmpty(eduList)) {
            throw new InfoException(InfoEnum.EDU_PARAMS_ERROR.getCode(), "教育经历不能为空");
        }
        Long userId = eduList.get(0).getUserId();
        // 先删除该用户所有教育经历
        this.remove(new LambdaQueryWrapper<Edu>().eq(Edu::getUserId, userId));
        for (int i = 0; i < eduList.size(); i++) {
            Edu edu = eduList.get(i);
            edu.setUserId(userId);
            edu.setPriority(eduList.size() - i);
            edu.setCreateTime(LocalDateTime.now());
            edu.setUpdateTime(LocalDateTime.now());
        }
        this.saveBatch(eduList);
        return eduList;
    }
}




