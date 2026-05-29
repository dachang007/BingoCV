package net.de5.yeoh.bingocv.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 个人简介及基本信息表
 * @TableName bingo_profiles
 */
@TableName(value ="bingo_profiles")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Profiles extends BaseDO implements Serializable {
    /**
     * 关联的用户账号ID
     */
    @TableField("userid")
    private Long userId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 免冠照
     */
    private String photo;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 地区城市
     */
    private String city;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 电话或手机号码
     */
    private String phone;

    /**
     * 微信号码
     */
    private String weixin;

    /**
     * qq号码
     */
    private String qq;

    /**
     * 微博地址
     */
    private String weibo;

    /**
     * 用户性别
     */
    private String sex;

    /**
     * 个人简介
     */
    private String description;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
