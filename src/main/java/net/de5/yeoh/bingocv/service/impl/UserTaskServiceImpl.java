package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.mapper.UserTaskMapper;
import net.de5.yeoh.bingocv.model.domain.UserTask;
import net.de5.yeoh.bingocv.service.UserTaskService;
import org.springframework.stereotype.Service;

@Service
public class UserTaskServiceImpl extends ServiceImpl<UserTaskMapper, UserTask> implements UserTaskService {
}
