package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.mapper.ShortUrlMapper;
import net.de5.yeoh.bingocv.model.domain.ShortUrl;
import net.de5.yeoh.bingocv.service.ShortUrlService;
import org.springframework.stereotype.Service;

@Service
public class ShortUrlServiceImpl extends ServiceImpl<ShortUrlMapper, ShortUrl> implements ShortUrlService {
}
