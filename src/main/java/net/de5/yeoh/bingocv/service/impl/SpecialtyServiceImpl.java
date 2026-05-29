package net.de5.yeoh.bingocv.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.model.domain.Specialty;
import net.de5.yeoh.bingocv.service.SpecialtyService;
import net.de5.yeoh.bingocv.mapper.SpecialtyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author yeoh
* @description 针对表【bingo_specialty(用于描述用户特长)】的数据库操作Service实现
* @createDate 2026-05-10 20:11:56
*/
@Service
public class SpecialtyServiceImpl extends ServiceImpl<SpecialtyMapper, Specialty>
    implements SpecialtyService{

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Specialty> add(List<Specialty> specialtyList) {
        if (CollUtil.isEmpty(specialtyList)) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "特长不能为空");
        }
        Long userId = specialtyList.get(0).getUserId();
        // 先删除该用户所有特长
        this.remove(new LambdaQueryWrapper<Specialty>().eq(Specialty::getUserId, userId));
        for (Specialty specialty : specialtyList) {
            specialty.setUserId(userId);
            specialty.setCreateTime(LocalDateTime.now());
            specialty.setUpdateTime(LocalDateTime.now());
        }
        this.saveBatch(specialtyList);
        return specialtyList;
    }
}
