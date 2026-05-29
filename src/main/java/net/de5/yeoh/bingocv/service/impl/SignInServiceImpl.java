package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.mapper.SignInMapper;
import net.de5.yeoh.bingocv.model.domain.SignIn;
import net.de5.yeoh.bingocv.service.SignInService;
import org.springframework.stereotype.Service;

@Service
public class SignInServiceImpl extends ServiceImpl<SignInMapper, SignIn> implements SignInService {
}
