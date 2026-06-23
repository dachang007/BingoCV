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
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share> implements ShareService {

    private static final String PUBLIC = "PUBLIC";
    private static final String PRIVATE = "PRIVATE";

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
    @Autowired
    private TaskCompletionService taskCompletionService;

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
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "分享配置不能为空");
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
        taskCompletionService.completeTask(userId, "share_resume");
        return ShareVO.from(share, publicBaseUrl());
    }

    /**
     * 更新分享配置
     * @param userId 用户id
     * @param id 简历分享id
     * @param request 分享配置请求参数
     * @return 分享配置详情VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareVO updateShare(Long userId, Long id, ShareUpdateRequest request) {
        Share share = getOwnedShare(userId, id);
        if (request == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "分享配置不能为空");
        }
        String type = normalizeType(request.getShareType());
        share.setShareType(type);
        share.setTitle(defaultTitle(request.getTitle()));

        // 编辑私密分享时，密码留空表示继续使用旧密码。
        if (PRIVATE.equals(type) && !StringUtils.hasText(request.getPassword()) && StringUtils.hasText(share.getPassword())) {
            share.setPassword(share.getPassword());
        } else {
            share.setPassword(normalizePassword(type, request.getPassword()));
        }

        share.setAccessLimit(normalizeAccessLimit(request.getAccessLimit()));
        share.setExpireTime(request.getExpireTime());
        if (request.getStatus() != null) {
            share.setStatus(request.getStatus());
        }
        this.updateById(share);
        syncShortUrl(share);
        return ShareVO.from(this.getById(id), publicBaseUrl());
    }

    /**
     * 关闭简历分享链接
     * @param userId 用户id
     * @param id 简历分享id
     * @return true if success
     */
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

    /**
     * 获取简历分享访问统计信息
     * @param userId 用户id
     * @param id 简历分享id
     * @return 简历分享访问统计信息VO
     */
    @Override
    public ShareAccessStatsVO accessStats(Long userId, Long id) {
        Share share = getOwnedShare(userId, id);
        List<ShareAccess> records = shareAccessService.list(new LambdaQueryWrapper<ShareAccess>()
                .eq(ShareAccess::getShareId, share.getId())
                .orderByDesc(ShareAccess::getCreateTime)
                .last("LIMIT 30"));
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        Long todayCount = shareAccessService.count(new LambdaQueryWrapper<ShareAccess>()
                .eq(ShareAccess::getShareId, share.getId())
                .ge(ShareAccess::getCreateTime, todayStart));
        Long uniqueVisitorCount = shareAccessService.list(new LambdaQueryWrapper<ShareAccess>()
                        .select(ShareAccess::getVisitorKey)
                        .eq(ShareAccess::getShareId, share.getId())
                        .groupBy(ShareAccess::getVisitorKey))
                .stream()
                .filter(item -> StringUtils.hasText(item.getVisitorKey()))
                .count();
        return ShareAccessStatsVO.builder()
                .shareId(share.getId())
                .accessCount(share.getAccessCount())
                .todayCount(todayCount)
                .uniqueVisitorCount(uniqueVisitorCount)
                .recentRecords(records)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PublicResumeVO accessPublicResume(String shortCode, ShareAccessRequest request) {
        Share share = getShareByCode(shortCode);
        validateAvailable(share);
        validatePassword(share, request);

        // 只有成功打开简历后才计数，避免密码错误也消耗访问次数。
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
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "短链接不可用");
        }
        if (shortUrl.getExpireTime() != null && shortUrl.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "短链接已过期");
        }
        shortUrlService.update(new LambdaUpdateWrapper<ShortUrl>()
                .eq(ShortUrl::getId, shortUrl.getId())
                .setSql("access_count = access_count + 1"));
        return shortUrl.getTargetUrl();
    }

    private Share getOwnedShare(Long userId, Long id) {
        if (id == null) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "分享ID不能为空");
        }
        Share share = this.getOne(new LambdaQueryWrapper<Share>()
                .eq(Share::getId, id)
                .eq(Share::getUserId, userId)
                .last("LIMIT 1"));
        if (share == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "分享不存在或不属于当前用户");
        }
        return share;
    }

    private Share getShareByCode(String shortCode) {
        if (!StringUtils.hasText(shortCode)) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "分享码不能为空");
        }
        Share share = this.getOne(new LambdaQueryWrapper<Share>()
                .eq(Share::getShortCode, shortCode)
                .last("LIMIT 1"));
        if (share == null) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "分享不存在");
        }
        return share;
    }

    private void validateAvailable(Share share) {
        if (!Integer.valueOf(1).equals(share.getStatus())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "分享已关闭");
        }
        if (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "分享已过期");
        }
        if (share.getAccessLimit() != null && share.getAccessCount() >= share.getAccessLimit()) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "分享访问次数已达上限");
        }
    }

    private void validatePassword(Share share, ShareAccessRequest request) {
        if (!PRIVATE.equals(share.getShareType())) {
            return;
        }
        String password = request == null ? null : request.getPassword();
        if (!StringUtils.hasText(password) || !Objects.equals(share.getPassword(), password.trim())) {
            throw new InfoException(InfoEnum.NO_AUTH_ERROR.getCode(), "访问密码为空或不正确");
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
                .region(resolveRegion(ip))
                .userAgent(userAgent)
                .build();
        shareAccessService.save(access);
    }

    private void createShortUrl(Share share) {
        shortUrlService.save(ShortUrl.builder()
                .shortCode(share.getShortCode())
                .targetUrl("/share/" + share.getShortCode())
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
        throw new InfoException(InfoEnum.OPERATION_ERROR.getCode(), "生成分享码失败");
    }

    private String normalizeType(String shareType) {
        if (!StringUtils.hasText(shareType)) {
            return PUBLIC;
        }
        String type = shareType.trim().toUpperCase(Locale.ROOT);
        if (!PUBLIC.equals(type) && !PRIVATE.equals(type)) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "分享类型只能是公开或私密");
        }
        return type;
    }

    private String normalizePassword(String type, String password) {
        if (!PRIVATE.equals(type)) {
            return null;
        }
        if (!StringUtils.hasText(password)) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "私密分享密码不能为空");
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
        return StringUtils.hasText(title) ? title.trim() : "我的 BingoCV 简历";
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

    private String resolveRegion(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "未知";
        }
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "本机访问";
        }
        if (ip.startsWith("10.") || ip.startsWith("192.168.") || ip.matches("^172\\.(1[6-9]|2\\d|3[0-1])\\..*")) {
            return "内网访问";
        }
        return "公网访问";
    }
}
