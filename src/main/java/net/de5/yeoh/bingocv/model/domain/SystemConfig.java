package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@TableName(value = "bingo_system_config")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig extends BaseDO implements Serializable {
    private String configKey;
    private String configValue;
    private String description;
    private Integer enabled;

    private static final long serialVersionUID = 1L;
}
