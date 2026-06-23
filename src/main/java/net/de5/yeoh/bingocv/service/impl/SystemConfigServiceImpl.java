package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.mapper.SystemConfigMapper;
import net.de5.yeoh.bingocv.model.domain.SystemConfig;
import net.de5.yeoh.bingocv.service.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public String getValue(String key, String defaultValue) {
        if (!StringUtils.hasText(key)) {
            return defaultValue;
        }
        SystemConfig config = this.getOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, key)
                .eq(SystemConfig::getEnabled, 1)
                .last("LIMIT 1"));
        return config == null || config.getConfigValue() == null ? defaultValue : config.getConfigValue();
    }

    @Override
    public Integer getIntValue(String key, Integer defaultValue) {
        try {
            return Integer.parseInt(getValue(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        String value = getValue(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
}
