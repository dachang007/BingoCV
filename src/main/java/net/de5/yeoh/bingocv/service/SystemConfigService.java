package net.de5.yeoh.bingocv.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.de5.yeoh.bingocv.model.domain.SystemConfig;

public interface SystemConfigService extends IService<SystemConfig> {
    String getValue(String key, String defaultValue);

    Integer getIntValue(String key, Integer defaultValue);

    Boolean getBooleanValue(String key, Boolean defaultValue);
}
