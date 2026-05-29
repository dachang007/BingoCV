package net.de5.yeoh.bingocv.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CaptchaService {

    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<>();

    private static class CacheEntry {
        final String value;
        final long expireTime;

        CacheEntry(String value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    private boolean isRedisAvailable() {
        if (redisTemplate == null) {
            return false;
        }
        try {
            redisTemplate.hasKey("__ping__");
            return true;
        } catch (Exception e) {
            log.warn("Redis不可用，使用本地缓存: {}", e.getMessage());
            return false;
        }
    }

    public String saveCaptcha(String captcha) {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String value = captcha.toLowerCase();

        if (isRedisAvailable()) {
            String key = CAPTCHA_KEY_PREFIX + captchaId;
            redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(CAPTCHA_EXPIRE_MINUTES));
            log.info("保存验证码到Redis: captchaId={}, captcha={}", captchaId, captcha);
        } else {
            long expireTime = System.currentTimeMillis() + CAPTCHA_EXPIRE_MINUTES * 60 * 1000;
            localCache.put(captchaId, new CacheEntry(value, expireTime));
            log.info("保存验证码到本地缓存: captchaId={}, captcha={}", captchaId, captcha);
        }

        return captchaId;
    }

    public boolean verifyCaptcha(String captchaId, String inputCaptcha) {
        if (captchaId == null || captchaId.isEmpty() || inputCaptcha == null || inputCaptcha.isEmpty()) {
            log.warn("验证码验证失败: captchaId或inputCaptcha为空");
            return false;
        }

        String storedCaptcha = null;

        if (isRedisAvailable()) {
            String key = CAPTCHA_KEY_PREFIX + captchaId;
            storedCaptcha = redisTemplate.opsForValue().get(key);
            if (storedCaptcha != null) {
                redisTemplate.delete(key);
            }
        } else {
            cleanupExpired();
            CacheEntry entry = localCache.remove(captchaId);
            if (entry != null && !entry.isExpired()) {
                storedCaptcha = entry.value;
            }
        }

        if (storedCaptcha == null) {
            log.warn("验证码验证失败: 验证码已过期或不存在, captchaId={}", captchaId);
            return false;
        }

        boolean matched = storedCaptcha.equalsIgnoreCase(inputCaptcha.trim());

        if (matched) {
            log.info("验证码验证成功, captchaId={}", captchaId);
        } else {
            log.warn("验证码验证失败: 不匹配, captchaId={}, stored={}, input={}", captchaId, storedCaptcha, inputCaptcha);
        }

        return matched;
    }

    private void cleanupExpired() {
        localCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
