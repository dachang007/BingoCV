package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.mapper.PayRefundMapper;
import net.de5.yeoh.bingocv.model.domain.PayRefund;
import net.de5.yeoh.bingocv.service.PayRefundService;
import org.springframework.stereotype.Service;

@Service
public class PayRefundServiceImpl extends ServiceImpl<PayRefundMapper, PayRefund> implements PayRefundService {
}
