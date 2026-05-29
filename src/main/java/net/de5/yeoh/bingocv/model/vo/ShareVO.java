package net.de5.yeoh.bingocv.model.vo;

import lombok.Builder;
import lombok.Data;
import net.de5.yeoh.bingocv.model.domain.Share;

import java.time.LocalDateTime;

@Data
@Builder
public class ShareVO {
    private Long id;
    private String shareType;
    private String title;
    private Integer accessLimit;
    private Integer accessCount;
    private LocalDateTime expireTime;
    private String shortCode;
    private Integer status;
    private String shareUrl;
    private String shortUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static ShareVO from(Share share, String publicBaseUrl) {
        String code = share.getShortCode();
        String base = publicBaseUrl == null ? "" : publicBaseUrl;
        return ShareVO.builder()
                .id(share.getId())
                .shareType(share.getShareType())
                .title(share.getTitle())
                .accessLimit(share.getAccessLimit())
                .accessCount(share.getAccessCount())
                .expireTime(share.getExpireTime())
                .shortCode(code)
                .status(share.getStatus())
                .shareUrl(base + "/s/" + code)
                .shortUrl(base + "/r/" + code)
                .createTime(share.getCreateTime())
                .updateTime(share.getUpdateTime())
                .build();
    }
}
