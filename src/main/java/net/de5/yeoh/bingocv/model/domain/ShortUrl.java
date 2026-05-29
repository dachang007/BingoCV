package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName(value = "bingo_short_url")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrl extends BaseDO implements Serializable {
    private String shortCode;
    private String targetUrl;
    private String bizType;
    private Long bizId;
    private Integer accessCount;
    private LocalDateTime expireTime;
    private Integer status;

    private static final long serialVersionUID = 1L;
}
