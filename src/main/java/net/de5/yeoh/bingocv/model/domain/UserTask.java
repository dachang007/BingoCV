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

@TableName(value = "bingo_user_task")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserTask extends BaseDO implements Serializable {
    @TableField("userid")
    private Long userId;
    private Long taskId;
    private Integer progress;
    private Integer completed;
    private Integer rewarded;
    private LocalDateTime completeTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
