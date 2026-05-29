package net.de5.yeoh.bingocv.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 基础VO类
 * @author: daChang
 * @date: 2026/5/10 19:20
 */
@Data
public class BaseVO {
    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
