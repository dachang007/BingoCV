package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.mapper.OperationLogMapper;
import net.de5.yeoh.bingocv.model.domain.OperationLog;
import net.de5.yeoh.bingocv.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
}
