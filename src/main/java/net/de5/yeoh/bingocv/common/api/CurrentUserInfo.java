package net.de5.yeoh.bingocv.common.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 当前用户信息
 * @author: daChang
 * @date: 2026/5/10 16:10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 在反序列化时忽略未知字段
public class CurrentUserInfo implements Serializable {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 角色或权限集合 (校验用)
     * 比如：admin, vip_user
     */
    private Set<String> roles;

    /**
     * 额外扩展字段 (可选)
     * 比如：当前的登录IP，或者该用户是否是付费会员（决定导出简历的模板权限）
     */
    private String loginIp;

    // private Boolean isVip;
}
