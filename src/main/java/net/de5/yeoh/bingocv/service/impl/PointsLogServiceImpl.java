package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.mapper.PointsLogMapper;
import net.de5.yeoh.bingocv.model.domain.PointsLog;
import net.de5.yeoh.bingocv.service.PointsLogService;
import org.springframework.stereotype.Service;

@Service
public class PointsLogServiceImpl extends ServiceImpl<PointsLogMapper, PointsLog> implements PointsLogService {
}
