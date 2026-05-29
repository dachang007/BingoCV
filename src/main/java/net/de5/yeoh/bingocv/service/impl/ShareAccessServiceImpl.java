package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.mapper.ShareAccessMapper;
import net.de5.yeoh.bingocv.model.domain.ShareAccess;
import net.de5.yeoh.bingocv.service.ShareAccessService;
import org.springframework.stereotype.Service;

@Service
public class ShareAccessServiceImpl extends ServiceImpl<ShareAccessMapper, ShareAccess> implements ShareAccessService {
}
