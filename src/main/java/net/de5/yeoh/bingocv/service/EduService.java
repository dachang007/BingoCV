package net.de5.yeoh.bingocv.service;

import net.de5.yeoh.bingocv.model.domain.Edu;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author yeoh
* @description 针对表【bingo_edu(用于描述用户的学习)】的数据库操作Service
* @createDate 2026-05-10 20:11:56
*/
import java.util.List;

public interface EduService extends IService<Edu> {

    /**
     * 保存用户的教育经历（先删除旧数据再批量保存）
     * @param eduList 教育经历列表
     * @return 保存后的列表
     */
    List<Edu> add(List<Edu> eduList);

    /**
     * 根据用户ID查询教育经历列表
     * @param userId 用户ID
     * @return 教育经历列表
     */
    List<Edu> listByUserId(Long userId);
}
