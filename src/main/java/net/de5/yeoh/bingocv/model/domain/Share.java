package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName(value = "bingo_share")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Share extends BaseDO implements Serializable {
    @TableField("userid")
    private Long userId;
    private String shareType;
    private String title;
    private String password;
    private Integer accessLimit;
    private Integer accessCount;
    private LocalDateTime expireTime;
    private String shortCode;
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
