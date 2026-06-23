package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.mapper.AiUsageLogMapper;
import net.de5.yeoh.bingocv.model.domain.AiUsageLog;
import net.de5.yeoh.bingocv.service.AiUsageLogService;
import org.springframework.stereotype.Service;

@Service
public class AiUsageLogServiceImpl extends ServiceImpl<AiUsageLogMapper, AiUsageLog> implements AiUsageLogService {
}
