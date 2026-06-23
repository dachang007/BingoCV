package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@TableName(value = "bingo_operation_log")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog extends BaseDO implements Serializable {
    @TableField("userid")
    private Long userId;
    private String username;
    private String module;
    private String action;
    private String requestMethod;
    private String requestUri;
    private String ip;
    private Integer status;
    private String errorMsg;
    private Long costMs;

    private static final long serialVersionUID = 1L;
}
