package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.mapper.UserTemplateMapper;
import net.de5.yeoh.bingocv.model.domain.UserTemplate;
import net.de5.yeoh.bingocv.service.UserTemplateService;
import org.springframework.stereotype.Service;

@Service
public class UserTemplateServiceImpl extends ServiceImpl<UserTemplateMapper, UserTemplate> implements UserTemplateService {
}
