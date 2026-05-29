package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@TableName(value = "bingo_share_access")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ShareAccess extends BaseDO implements Serializable {
    private Long shareId;
    private String visitorKey;
    private String ip;
    private String region;
    private String userAgent;

    private static final long serialVersionUID = 1L;
}
