package net.de5.yeoh.bingocv.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareCreateRequest {
    private String shareType;
    private String title;
    private String password;
    private Integer accessLimit;
    private LocalDateTime expireTime;
}
