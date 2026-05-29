package net.de5.yeoh.bingocv.common.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Kaptcha验证码配置类
 */
@Configuration
public class KaptchaConfig {

    @Bean
    public DefaultKaptcha captchaProducer() {
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        
        // 图片宽度
        properties.setProperty("kaptcha.image.width", "120");
        // 图片高度
        properties.setProperty("kaptcha.image.height", "40");
        // 字符集
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        // 字符长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "30");
        // 字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        // 字符间距
        properties.setProperty("kaptcha.textproducer.char.space", "5");
        // 干扰线颜色
        properties.setProperty("kaptcha.noise.color", "blue");
        // 边框
        properties.setProperty("kaptcha.border", "yes");
        properties.setProperty("kaptcha.border.color", "105,179,90");
        // 背景颜色渐变开始
        properties.setProperty("kaptcha.background.clear.from", "211,211,211");
        // 背景颜色渐变结束
        properties.setProperty("kaptcha.background.clear.to", "255,255,255");
        
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}