package net.de5.yeoh.bingocv.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "简历导出请求")
public class ExportResumeDTO {

    @Schema(description = "导出格式：pdf、word、markdown")
    private String format;

    @Schema(description = "模板标识")
    private String templateKey;

    @Schema(description = "用户ID，管理员导出时使用")
    private Long userId;
}
