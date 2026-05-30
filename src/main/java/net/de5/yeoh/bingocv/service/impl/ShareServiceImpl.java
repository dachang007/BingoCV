package net.de5.yeoh.bingocv.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.RequestUtils;
import net.de5.yeoh.bingocv.mapper.ShareMapper;
import net.de5.yeoh.bingocv.model.domain.*;
import net.de5.yeoh.bingocv.model.dto.ShareAccessRequest;
import net.de5.yeoh.bingocv.model.dto.ShareCreateRequest;
import net.de5.yeoh.bingocv.model.dto.ShareUpdateRequest;
import net.de5.yeoh.bingocv.model.vo.PublicResumeVO;
import net.de5.yeoh.bingocv.model.vo.ShareAccessStatsVO;
import net.de5.yeoh.bingocv.model.vo.ShareVO;
import net.de5.yeoh.bingocv.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share> implements ShareService {

    // 分享类型常量
    private static final String PUBLIC = "PUBLIC";   // 公开分享：无需密码即可访问
    private static final String PRIVATE = "PRIVATE"; // 私密分享：需要密码才能访问

    @Autowired
    private ShareAccessService shareAccessService;
    @Autowired
    private ShortUrlService shortUrlService;
    @Autowired
    private ProfilesService profilesService;
    @Autowired
    private EduService eduService;
    @Autowired
    private WorkService workService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private SpecialtyService specialtyService;

    @Override
    public List<ShareVO> myShares(Long userId) {
        return this.list(new LambdaQueryWrapper<Share>()
                        .eq(Share::getUserId, userId)
                        .orderByDesc(Share::getCreateTime))
                .stream()
                .map(share -> ShareVO.from(share, publicBaseUrl()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareVO createShare(Long userId, ShareCreateRequest request) {
        if (request == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "Share config cannot be empty");
        }
        String type = normalizeType(request.getShareType());
        String shortCode = nextShortCode();
        Share share = Share.builder()
                .userId(userId)
                .shareType(type)
                .title(defaultTitle(request.getTitle()))
                .password(normalizePassword(type, request.getPassword()))
                .accessLimit(normalizeAccessLimit(request.getAccessLimit()))
                .accessCount(0)
                .expireTime(request.getExpireTime())
                .shortCode(shortCode)
                .status(1)
                .build();
        this.save(share);
        createShortUrl(share);
        return ShareVO.from(share, publicBaseUrl());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareVO updateShare(Long userId, Long id, ShareUpdateRequest request) {
        Share share = getOwnedShare(userId, id);
        if (request == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "Share config cannot be empty");
        }
        String type = normalizeType(request.getShareType());
        share.setShareType(type);
        share.setTitle(defaultTitle(request.getTitle()));
        share.setPassword(normalizePassword(type, request.getPassword()));
        share.setAccessLimit(normalizeAccessLimit(request.getAccessLimit()));
        share.setExpireTime(request.getExpireTime());
        if (request.getStatus() != null) {
            share.setStatus(request.getStatus());
        }
        this.updateById(share);
        syncShortUrl(share);
        return ShareVO.from(this.getById(id), publicBaseUrl());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean closeShare(Long userId, Long id) {
        Share share = getOwnedShare(userId, id);
        share.setStatus(0);
        this.updateById(share);
        shortUrlService.update(new LambdaUpdateWrapper<ShortUrl>()
                .eq(ShortUrl::getBizType, "SHARE")
                .eq(ShortUrl::getBizId, share.getId())
                .set(ShortUrl::getStatus, 0));
        return true;
    }

    @Override
    public ShareAccessStatsVO accessStats(Long userId, Long id) {
        Share share = getOwnedShare(userId, id);
        List<ShareAccess> records = shareAccessService.list(new LambdaQueryWrapper<ShareAccess>()
                .eq(ShareAccess::getShareId, share.getId())
                .orderByDesc(ShareAccess::getCreateTime)
                .last("LIMIT 30"));
        return ShareAccessStatsVO.builder()
                .shareId(share.getId())
                .accessCount(share.getAccessCount())
                .recentRecords(records)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PublicResumeVO accessPublicResume(String shortCode, ShareAccessRequest request) {
        Share share = getShareByCode(shortCode);
        validateAvailable(share);
        validatePassword(share, request);

        // Count every successful open; this powers visit limits and analytics.
        recordAccess(share);
        this.update(new LambdaUpdateWrapper<Share>()
                .eq(Share::getId, share.getId())
                .setSql("access_count = access_count + 1"));

        Long ownerId = share.getUserId();
        return PublicResumeVO.builder()
                .share(ShareVO.from(this.getById(share.getId()), publicBaseUrl()))
                .profile(profilesService.getByUserId(ownerId))
                .educationList(eduService.list(new LambdaQueryWrapper<Edu>()
                        .eq(Edu::getUserId, ownerId)
                        .orderByDesc(Edu::getPriority)
                        .orderByDesc(Edu::getUpdateTime)))
                .workList(workService.list(new LambdaQueryWrapper<Work>()
                        .eq(Work::getUserId, ownerId)
                        .orderByDesc(Work::getPriority)
                        .orderByDesc(Work::getUpdateTime)))
                .skill(skillService.getOne(new LambdaQueryWrapper<Skill>()
                        .eq(Skill::getUserId, ownerId)
                        .last("LIMIT 1")))
                .specialtyList(specialtyService.list(new LambdaQueryWrapper<Specialty>()
                        .eq(Specialty::getUserId, ownerId)
                        .orderByDesc(Specialty::getUpdateTime)))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String redirectTarget(String shortCode) {
        ShortUrl shortUrl = shortUrlService.getOne(new LambdaQueryWrapper<ShortUrl>()
                .eq(ShortUrl::getShortCode, shortCode)
                .last("LIMIT 1"));
        if (shortUrl == null || !Integer.valueOf(1).equals(shortUrl.getStatus())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "Short link is unavailable");
        }
        if (shortUrl.getExpireTime() != null && shortUrl.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "Short link has expired");
        }
        shortUrlService.update(new LambdaUpdateWrapper<ShortUrl>()
                .eq(ShortUrl::getId, shortUrl.getId())
                .setSql("access_count = access_count + 1"));
        return shortUrl.getTargetUrl();
    }

    private Share getOwnedShare(Long userId, Long id) {
        if (id == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "Share id cannot be empty");
        }
        Share share = this.getOne(new LambdaQueryWrapper<Share>()
                .eq(Share::getId, id)
                .eq(Share::getUserId, userId)
                .last("LIMIT 1"));
        if (share == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "Share does not exist");
        }
        return share;
    }

    private Share getShareByCode(String shortCode) {
        if (!StringUtils.hasText(shortCode)) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "Share code cannot be empty");
        }
        Share share = this.getOne(new LambdaQueryWrapper<Share>()
                .eq(Share::getShortCode, shortCode)
                .last("LIMIT 1"));
        if (share == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "Share does not exist");
        }
        return share;
    }

    private void validateAvailable(Share share) {
        if (!Integer.valueOf(1).equals(share.getStatus())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "Share is closed");
        }
        if (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "Share has expired");
        }
        if (share.getAccessLimit() != null && share.getAccessCount() >= share.getAccessLimit()) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "Share access limit reached");
        }
    }

    private void validatePassword(Share share, ShareAccessRequest request) {
        if (!PRIVATE.equals(share.getShareType())) {
            return;
        }
        String password = request == null ? null : request.getPassword();
        if (!StringUtils.hasText(password) || !Objects.equals(share.getPassword(), password.trim())) {
            throw new InfoException(InfoEnum.NO_AUTH_ERROR.getCode(), "Password is required or incorrect");
        }
    }

    private void recordAccess(Share share) {
        HttpServletRequest request = RequestUtils.getRequest();
        String ip = clientIp(request);
        String userAgent = request == null ? null : request.getHeader("User-Agent");
        ShareAccess access = ShareAccess.builder()
                .shareId(share.getId())
                .visitorKey(SecureUtil.md5((ip == null ? "" : ip) + "|" + (userAgent == null ? "" : userAgent)))
                .ip(ip)
                .region("Unknown")
                .userAgent(userAgent)
                .build();
        shareAccessService.save(access);
    }

    private void createShortUrl(Share share) {
        shortUrlService.save(ShortUrl.builder()
                .shortCode(share.getShortCode())
                .targetUrl("/s/" + share.getShortCode())
                .bizType("SHARE")
                .bizId(share.getId())
                .accessCount(0)
                .expireTime(share.getExpireTime())
                .status(share.getStatus())
                .build());
    }

    private void syncShortUrl(Share share) {
        shortUrlService.update(new LambdaUpdateWrapper<ShortUrl>()
                .eq(ShortUrl::getBizType, "SHARE")
                .eq(ShortUrl::getBizId, share.getId())
                .set(ShortUrl::getExpireTime, share.getExpireTime())
                .set(ShortUrl::getStatus, share.getStatus()));
    }

    private String nextShortCode() {
        for (int i = 0; i < 8; i++) {
            String code = IdUtil.fastSimpleUUID().substring(0, 8);
            long count = this.count(new LambdaQueryWrapper<Share>().eq(Share::getShortCode, code));
            if (count == 0) {
                return code;
            }
        }
        throw new InfoException(InfoEnum.OPERATION_ERROR.getCode(), "Failed to generate share code");
    }

    private String normalizeType(String shareType) {
        if (!StringUtils.hasText(shareType)) {
            return PUBLIC;
        }
        String type = shareType.trim().toUpperCase(Locale.ROOT);
        if (!PUBLIC.equals(type) && !PRIVATE.equals(type)) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "Share type must be PUBLIC or PRIVATE");
        }
        return type;
    }

    private String normalizePassword(String type, String password) {
        if (!PRIVATE.equals(type)) {
            return null;
        }
        if (!StringUtils.hasText(password)) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "Private share password cannot be empty");
        }
        return password.trim();
    }

    private Integer normalizeAccessLimit(Integer accessLimit) {
        if (accessLimit == null || accessLimit <= 0) {
            return null;
        }
        return accessLimit;
    }

    private String defaultTitle(String title) {
        return StringUtils.hasText(title) ? title.trim() : "My BingoCV Resume";
    }

    private String publicBaseUrl() {
        HttpServletRequest request = RequestUtils.getRequest();
        if (request == null) {
            return "";
        }
        return request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return StringUtils.hasText(realIp) ? realIp : request.getRemoteAddr();
    }
}
