package net.de5.yeoh.bingocv.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.de5.yeoh.bingocv.model.domain.Points;
import net.de5.yeoh.bingocv.model.domain.PointsLog;
import net.de5.yeoh.bingocv.model.domain.SignIn;
import net.de5.yeoh.bingocv.model.domain.Task;

import java.util.List;
import java.util.Map;

public interface PointsService extends IService<Points> {
    Points getOrCreate(Long userId);

    Points change(Long userId, int amount, String bizType, Long bizId, String remark);

    Map<String, Object> dashboard(Long userId);

    List<PointsLog> logs(Long userId);

    List<Task> tasks(Long userId);

    SignIn signIn(Long userId);
}
