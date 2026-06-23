package net.de5.yeoh.bingocv.service;

import net.de5.yeoh.bingocv.model.domain.Work;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yeoh
* @description 针对表【bingo_work(用于描述用户的工作经历)】的数据库操作Service
* @createDate 2026-05-10 20:11:56
*/
public interface WorkService extends IService<Work> {

    /**
     * 添加工作经历
     * @param workList 工作经历列表
     * @return 保存后的列表
     */
    List<Work> add(List<Work> workList);

    /**
     * 根据用户ID查询工作经历列表
     * @param userId 用户ID
     * @return 工作经历列表
     */
    List<Work> listByUserId(Long userId);
}
