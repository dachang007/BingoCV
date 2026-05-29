package net.de5.yeoh.bingocv.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.de5.yeoh.bingocv.service.impl.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 验证码接口
 * @author: daChang
 * @date: 2026/5/12 10:00
 */
@RestController
@RequestMapping("/captcha")
@Slf4j
@Tag(name = "验证码接口")
public class CaptchaController {

    @Autowired
    private DefaultKaptcha captchaProducer;

    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/image")
    @Operation(summary = "获取验证码图片")
    public void getCaptchaImage(HttpServletResponse response) throws IOException {
        // 设置响应头
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");

        // 生成验证码文本
        String captchaText = captchaProducer.createText();

        // 将验证码存储到Redis，返回captchaId
        String captchaId = captchaService.saveCaptcha(captchaText);

        // 将captchaId放到响应头中，前端获取
        response.setHeader("X-Captcha-Id", captchaId);

        // 生成验证码图片
        BufferedImage captchaImage = captchaProducer.createImage(captchaText);

        // 输出图片
        ImageIO.write(captchaImage, "jpg", response.getOutputStream());
    }
}
