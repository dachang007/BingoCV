package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;

@TableName(value = "bingo_template")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Template extends BaseDO implements Serializable {
    private String name;
    private String industry;
    private String coverUrl;
    private String templateKey;
    private String templateType;
    private Integer pointsCost;
    private BigDecimal price;
    private String description;
    private Integer status;
    private Integer sortOrder;

    @TableField(exist = false)
    private Boolean owned;

    @TableField(exist = false)
    private Boolean active;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
