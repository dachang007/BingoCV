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
        /**
         * 业务逻辑：
         * 1. 参数校验：检查特长列表是否为空
         * 2. 获取当前用户ID（从列表第一条记录获取）
         * 3. 删除该用户已存在的所有特长记录
         * 4. 为每条特长记录设置用户ID、创建时间和更新时间
         * 5. 批量保存新的特长记录
         * 
         * 设计说明：
         * - 用户特长支持多条记录，每条记录代表一项特长
         * - 采用"删除旧数据+批量插入"的策略，简化前端操作逻辑
         * - 使用事务确保删除和保存操作的原子性，避免数据不一致
         */
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

    @Override
    public List<Specialty> listByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Specialty>()
                .eq(Specialty::getUserId, userId));
    }
}
