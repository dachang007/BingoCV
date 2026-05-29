package net.de5.yeoh.bingocv.service;

import net.de5.yeoh.bingocv.model.domain.Specialty;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yeoh
* @description 针对表【bingo_specialty(用于描述用户特长)】的数据库操作Service
* @createDate 2026-05-10 20:11:56
*/
public interface SpecialtyService extends IService<Specialty> {

    /**
     * 添加特长
     * @param specialtyList 特长列表
     * @return 保存后的列表
     */
    List<Specialty> add(List<Specialty> specialtyList);
}
