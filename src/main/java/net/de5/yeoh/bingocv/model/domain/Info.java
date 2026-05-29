package net.de5.yeoh.bingocv.model.domain;

import lombok.*;

import java.util.List;

/**
 * 最后生成的简历（聚合VO）
 * @author: daChang
 * @date: 2026/5/10 20:17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Info {

    /**
     * 教育经历列表
     */
    private List<Edu> eduList;

    /**
     * 个人技能
     */
    private Skill skill;

    /**
     * 特长列表
     */
    private List<Specialty> specialtyList;

    /**
     * 工作经历列表
     */
    private List<Work> workList;

    /**
     * 用户个人简介
     */
    private Profiles profiles;
}
