package net.de5.yeoh.bingocv.service.impl;

import cn.hutool.core.util.StrUtil;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.model.domain.AiUsageLog;
import net.de5.yeoh.bingocv.model.domain.Edu;
import net.de5.yeoh.bingocv.model.domain.Profiles;
import net.de5.yeoh.bingocv.model.domain.Skill;
import net.de5.yeoh.bingocv.model.domain.Specialty;
import net.de5.yeoh.bingocv.model.domain.Work;
import net.de5.yeoh.bingocv.model.vo.AiJdMatchVO;
import net.de5.yeoh.bingocv.model.vo.AiResumeAgentVO;
import net.de5.yeoh.bingocv.model.vo.AiResumeAnalysisVO;
import net.de5.yeoh.bingocv.service.AiResumeService;
import net.de5.yeoh.bingocv.service.AiUsageLogService;
import net.de5.yeoh.bingocv.service.EduService;
import net.de5.yeoh.bingocv.service.ProfilesService;
import net.de5.yeoh.bingocv.service.SkillService;
import net.de5.yeoh.bingocv.service.SpecialtyService;
import net.de5.yeoh.bingocv.service.SystemConfigService;
import net.de5.yeoh.bingocv.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AiResumeServiceImpl implements AiResumeService {

    private static final List<String> COMMON_JD_KEYWORDS = List.of(
            "Java", "Spring Boot", "Spring Cloud", "MySQL", "Redis", "Vue", "React", "TypeScript",
            "JavaScript", "Docker", "Kubernetes", "Linux", "Nginx", "微服务", "分布式", "高并发",
            "性能优化", "项目管理", "沟通协作", "数据分析", "产品设计", "用户增长", "运营", "测试"
    );

    @Autowired
    private ProfilesService profilesService;
    @Autowired
    private WorkService workService;
    @Autowired
    private EduService eduService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private SpecialtyService specialtyService;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private AiUsageLogService aiUsageLogService;

    @Override
    public AiResumeAnalysisVO analyze(Long userId) {
        long start = System.currentTimeMillis();
        ResumeSnapshot snapshot = loadSnapshot(userId);
        AiResumeAnalysisVO result = buildLocalAnalysis(snapshot);
        recordUsage(userId, "ANALYZE", true, true, System.currentTimeMillis() - start, null);
        return result;
    }

    @Override
    public AiResumeAgentVO agentPlan(Long userId) {
        long start = System.currentTimeMillis();
        ResumeSnapshot snapshot = loadSnapshot(userId);
        AiResumeAnalysisVO analysis = buildLocalAnalysis(snapshot);
        List<AiResumeAgentVO.AgentAction> actions = buildAgentActions(snapshot, analysis);
        int readiness = cap(analysis.getScore() == null ? 0 : analysis.getScore());
        recordUsage(userId, "AGENT_PLAN", true, true, System.currentTimeMillis() - start, null);
        return AiResumeAgentVO.builder()
                .readiness(readiness)
                .stage(readiness >= 85 ? "可投递" : readiness >= 70 ? "建议优化后投递" : "需要补齐核心信息")
                .summary("AI Agent 找到 " + actions.size() + " 个可执行优化动作，当前投递准备度为 " + readiness + " 分。")
                .actions(actions)
                .reminders(buildAgentReminders(snapshot, readiness))
                .build();
    }

    @Override
    public AiJdMatchVO matchJd(Long userId, String jobTitle, String jdContent) {
        long start = System.currentTimeMillis();
        if (!StringUtils.hasText(jdContent)) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "请先粘贴招聘 JD 内容");
        }
        ResumeSnapshot snapshot = loadSnapshot(userId);
        AiJdMatchVO result = buildLocalJdMatch(snapshot, jobTitle, jdContent);
        recordUsage(userId, "JD_MATCH", true, true, System.currentTimeMillis() - start, null);
        return result;
    }

    @Override
    public String polish(Long userId, String scene, String content) {
        long start = System.currentTimeMillis();
        if (!StringUtils.hasText(content)) {
            recordUsage(userId, "POLISH", false, true, System.currentTimeMillis() - start, "内容为空");
            return "请先输入需要润色的简历内容。";
        }
        String result = localPolish(scene, content);
        recordUsage(userId, "POLISH", true, true, System.currentTimeMillis() - start, null);
        return result;
    }

    private ResumeSnapshot loadSnapshot(Long userId) {
        return new ResumeSnapshot(
                profilesService.getByUserId(userId),
                sortedWorks(workService.listByUserId(userId)),
                eduService.listByUserId(userId),
                skillService.getByUserId(userId),
                specialtyService.listByUserId(userId)
        );
    }

    private AiResumeAnalysisVO buildLocalAnalysis(ResumeSnapshot snapshot) {
        List<String> highlights = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        List<String> polished = new ArrayList<>();
        List<AiResumeAnalysisVO.SectionScore> sections = new ArrayList<>();

        int profileScore = scoreProfile(snapshot.profile(), problems, suggestions, highlights);
        int workScore = scoreWork(snapshot.works(), problems, suggestions, highlights, polished);
        int eduScore = scoreEducation(snapshot.edus(), problems, suggestions);
        int skillScore = scoreSkill(snapshot.skill(), problems, suggestions, highlights);
        int specialtyScore = scoreSpecialty(snapshot.specialties(), suggestions);
        int score = Math.round(profileScore * 0.2f + workScore * 0.35f + eduScore * 0.15f + skillScore * 0.2f + specialtyScore * 0.1f);

        sections.add(section("基础信息", profileScore));
        sections.add(section("工作经历", workScore));
        sections.add(section("教育经历", eduScore));
        sections.add(section("技能关键词", skillScore));
        sections.add(section("个人特长", specialtyScore));

        if (polished.isEmpty()) {
            polished.add("建议先补充 1-2 段可量化的项目或工作成果，AI 润色会更准确。");
        }

        return AiResumeAnalysisVO.builder()
                .score(score)
                .level(level(score))
                .summary(summary(snapshot.profile(), score, snapshot.works()))
                .highlights(defaultList(highlights, "简历基础结构已经建立，可以继续补充成果数据来增强说服力。"))
                .problems(defaultList(problems, "暂未发现明显结构性问题。"))
                .suggestions(defaultList(suggestions, "保持每段经历包含职责、行动、结果三个要素。"))
                .polishedBullets(polished)
                .interviewQuestions(buildInterviewQuestions(snapshot.profile(), snapshot.works(), snapshot.skill()))
                .sectionScores(sections)
                .build();
    }

    private AiJdMatchVO buildLocalJdMatch(ResumeSnapshot snapshot, String jobTitle, String jdContent) {
        Set<String> jdKeywords = extractJdKeywords(jdContent);
        Set<String> resumeKeywords = extractResumeKeywords(snapshot);
        List<String> matched = jdKeywords.stream().filter(keyword -> containsIgnoreCase(resumeKeywords, keyword)).toList();
        List<String> missing = jdKeywords.stream().filter(keyword -> !containsIgnoreCase(resumeKeywords, keyword)).limit(12).toList();
        int base = jdKeywords.isEmpty() ? 45 : Math.round(matched.size() * 100f / jdKeywords.size());
        int matchScore = cap(base + (snapshot.works().isEmpty() ? -10 : 8));

        return AiJdMatchVO.builder()
                .matchScore(matchScore)
                .level(matchLevel(matchScore))
                .summary("目标岗位“" + StrUtil.blankToDefault(jobTitle, "未命名岗位") + "”匹配度为 " + matchScore + " 分，建议优先补齐缺失关键词和相关成果。")
                .matchedKeywords(defaultList(matched, "暂未命中明显关键词"))
                .missingKeywords(defaultList(missing, "暂无明显缺失关键词"))
                .strengths(buildJdStrengths(snapshot, matched))
                .risks(buildJdRisks(snapshot, missing))
                .rewriteSuggestions(buildJdRewriteSuggestions(jobTitle, missing))
                .interviewFocus(buildJdInterviewFocus(jobTitle, matched, missing))
                .build();
    }

    private List<AiResumeAgentVO.AgentAction> buildAgentActions(ResumeSnapshot snapshot, AiResumeAnalysisVO analysis) {
        List<AiResumeAgentVO.AgentAction> actions = new ArrayList<>();
        Profiles profile = snapshot.profile();
        if (profile == null || !hasText(profile.getName()) || !hasText(profile.getPhone()) || !hasText(profile.getEmail())) {
            actions.add(action("补齐联系方式", "完善姓名、手机号、邮箱和城市，让招聘方能快速判断并联系你。", "HIGH",
                    "基础信息不完整会直接影响投递可用性。", "简历可投递性明显提升", "profiles", "去完善"));
        }
        if (profile == null || !hasText(profile.getDescription()) || profile.getDescription().length() < 60) {
            actions.add(action("优化个人简介", "写一段 80-150 字的求职摘要，包含目标岗位、核心技能和代表成果。", "HIGH",
                    "个人简介是分享页和导出简历的第一屏信息。", "提升首屏吸引力", "profiles", "去填写"));
        }
        if (snapshot.works().isEmpty()) {
            actions.add(action("添加工作或项目经历", "至少补充一段最近经历，写清公司或项目、岗位、时间和结果。", "HIGH",
                    "没有经历会让 AI 无法判断岗位匹配度。", "简历主体完整度大幅提升", "work", "去添加"));
        } else if (analysis.getProblems().stream().anyMatch(item -> item.contains("经历"))) {
            actions.add(action("强化经历描述", "为每段经历补充背景、行动和结果，尽量加入数字化成果。", "HIGH",
                    "经历描述偏短，缺少结果信息。", "提升专业可信度和面试追问质量", "work", "去优化"));
        }
        if (snapshot.skill() == null || !hasText(snapshot.skill().getKeywords()) || skillCount(snapshot.skill()) < 5) {
            actions.add(action("补充技能关键词", "按语言、框架、工具、业务能力分组补充 8-12 个关键词。", "MEDIUM",
                    "技能关键词会影响模板展示、AI 诊断和 JD 匹配。", "提升岗位匹配和搜索命中", "skill", "去补充"));
        }
        if (actions.isEmpty()) {
            actions.add(action("创建简历分享", "生成可控制访问次数的简历分享链接，用于投递和内推。", "LOW",
                    "分享链接便于跟踪访问数据。", "提升投递跟进效率", "share", "去创建"));
        }
        return actions.stream().sorted(Comparator.comparingInt(item -> priorityWeight(item.getPriority()))).toList();
    }

    private AiResumeAgentVO.AgentAction action(String title, String description, String priority,
                                               String reason, String expectedImpact, String routeName, String buttonText) {
        return AiResumeAgentVO.AgentAction.builder()
                .title(title)
                .description(description)
                .priority(priority)
                .reason(reason)
                .expectedImpact(expectedImpact)
                .routeName(routeName)
                .buttonText(buttonText)
                .build();
    }

    private List<String> buildAgentReminders(ResumeSnapshot snapshot, int readiness) {
        List<String> reminders = new ArrayList<>();
        reminders.add(readiness < 70 ? "建议先补齐高优先级动作，再进行 JD 匹配和深度润色。" : "可以开始针对目标岗位做关键词适配，避免一份简历投所有岗位。");
        if (!snapshot.works().isEmpty()) {
            reminders.add("经历内容尽量用“动作 + 方法 + 结果”表达，结果最好包含数字。");
        }
        return reminders;
    }

    private List<String> buildJdStrengths(ResumeSnapshot snapshot, List<String> matched) {
        List<String> strengths = new ArrayList<>();
        if (!matched.isEmpty()) {
            strengths.add("简历已覆盖 JD 中的部分关键词：" + String.join("、", matched.stream().limit(6).toList()));
        }
        if (!snapshot.works().isEmpty()) {
            strengths.add("已有工作或项目经历，可以围绕 JD 要求进一步改写成果表达。");
        }
        return defaultList(strengths, "当前简历与 JD 的显性匹配点较少，需要先补充经历和技能关键词。");
    }

    private List<String> buildJdRisks(ResumeSnapshot snapshot, List<String> missing) {
        List<String> risks = new ArrayList<>();
        if (!missing.isEmpty()) {
            risks.add("JD 关键词未充分覆盖：" + String.join("、", missing.stream().limit(8).toList()));
        }
        if (snapshot.skill() == null || !hasText(snapshot.skill().getKeywords())) {
            risks.add("技能关键词为空，会降低岗位匹配判断和简历筛选通过率。");
        }
        return defaultList(risks, "暂无明显风险，但仍建议按 JD 调整关键词顺序和成果表达。");
    }

    private List<String> buildJdRewriteSuggestions(String jobTitle, List<String> missing) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("在个人简介中明确目标岗位“" + StrUtil.blankToDefault(jobTitle, "目标岗位") + "”，并放入 2-3 个核心关键词。");
        suggestions.add("把工作经历改写为“场景 + 行动 + 工具/方法 + 量化结果”，优先贴合 JD 的职责描述。");
        if (!missing.isEmpty()) {
            suggestions.add("如果你真实具备这些能力，可补充到技能关键词或经历描述中：" + String.join("、", missing.stream().limit(6).toList()));
        }
        return suggestions;
    }

    private List<String> buildJdInterviewFocus(String jobTitle, List<String> matched, List<String> missing) {
        List<String> focus = new ArrayList<>();
        focus.add("准备一个 2 分钟自我介绍，围绕“" + StrUtil.blankToDefault(jobTitle, "目标岗位") + "”说明匹配原因。");
        if (!matched.isEmpty()) {
            focus.add("重点准备已命中关键词的项目案例：" + String.join("、", matched.stream().limit(4).toList()));
        }
        if (!missing.isEmpty()) {
            focus.add("对缺失关键词准备诚实回答：是否做过、做到什么程度、如何补齐。");
        }
        return focus;
    }

    private int scoreProfile(Profiles profile, List<String> problems, List<String> suggestions, List<String> highlights) {
        if (profile == null) {
            problems.add("还没有填写个人基础信息。");
            suggestions.add("先完善姓名、城市、邮箱、手机号和个人简介。");
            return 15;
        }
        int score = 30;
        score += hasText(profile.getName()) ? 15 : missing(problems, "姓名未填写。");
        score += hasText(profile.getEmail()) ? 15 : missing(problems, "邮箱未填写。");
        score += hasText(profile.getPhone()) ? 15 : missing(problems, "手机号未填写。");
        score += hasText(profile.getCity()) ? 10 : missing(problems, "城市未填写。");
        if (hasText(profile.getDescription())) {
            score += profile.getDescription().length() >= 40 ? 15 : 8;
            highlights.add("已经填写个人简介，可继续强化岗位关键词和核心优势。");
        } else {
            problems.add("个人简介为空，分享页面和导出简历会显得比较单薄。");
            suggestions.add("个人简介建议控制在 80-150 字，写清目标岗位、经验年限、核心技能和代表成果。");
        }
        return cap(score);
    }

    private int scoreWork(List<Work> works, List<String> problems, List<String> suggestions, List<String> highlights, List<String> polished) {
        if (works == null || works.isEmpty()) {
            problems.add("还没有填写工作经历或项目经历。");
            suggestions.add("至少补充一段最近的工作或项目经历，包含公司、岗位、时间和成果。");
            return 20;
        }
        int score = Math.min(45 + works.size() * 10, 70);
        for (Work work : works) {
            if (hasText(work.getCompany()) && hasText(work.getJob())) {
                highlights.add("工作经历包含公司与岗位信息，结构基础较完整。");
            }
            if (hasText(work.getDescription())) {
                score += work.getDescription().length() >= 80 ? 10 : 4;
                polished.add(polishWork(work));
            } else {
                problems.add(label(work.getCompany(), "某段工作经历") + "缺少工作描述。");
            }
        }
        if (polished.stream().noneMatch(this::containsMetric)) {
            suggestions.add("工作经历建议加入数字化结果，例如效率提升、用户增长、成本下降、交付周期等。");
        }
        return cap(score);
    }

    private int scoreEducation(List<Edu> edus, List<String> problems, List<String> suggestions) {
        if (edus == null || edus.isEmpty()) {
            suggestions.add("教育经历可以简短填写学校、专业和就读时间，提升简历可信度。");
            return 45;
        }
        int score = 55;
        for (Edu edu : edus) {
            score += hasText(edu.getSchool()) ? 12 : missing(problems, "教育经历缺少学校名称。");
            score += hasText(edu.getStudy()) ? 8 : 0;
            score += hasText(edu.getStart()) || hasText(edu.getEnd()) ? 5 : 0;
        }
        return cap(score);
    }

    private int scoreSkill(Skill skill, List<String> problems, List<String> suggestions, List<String> highlights) {
        if (skill == null || !hasText(skill.getKeywords())) {
            problems.add("技能关键词为空，系统无法判断你的岗位匹配度。");
            suggestions.add("技能建议按“语言/框架/工具/业务能力”分组填写。");
            return 20;
        }
        int count = skillCount(skill);
        highlights.add("技能关键词已填写，可用于模板展示和面试问题生成。");
        if (count < 5) {
            suggestions.add("技能关键词偏少，建议补充 8-12 个与目标岗位直接相关的关键词。");
        }
        return cap(Math.min(35 + count * 8, 90));
    }

    private int scoreSpecialty(List<Specialty> specialties, List<String> suggestions) {
        if (specialties == null || specialties.isEmpty()) {
            suggestions.add("个人特长不是必填项，但可以放证书、开源项目、作品集或语言能力。");
            return 50;
        }
        return cap(60 + specialties.size() * 10);
    }

    private List<String> buildInterviewQuestions(Profiles profile, List<Work> works, Skill skill) {
        List<String> questions = new ArrayList<>();
        String target = profile == null || !hasText(profile.getDescription()) ? "目标岗位" : "你简历中提到的核心方向";
        questions.add("请用 2 分钟介绍一下自己，并突出你和" + target + "最匹配的能力。");
        if (works != null && !works.isEmpty()) {
            Work latest = works.get(0);
            questions.add("你在“" + label(latest.getCompany(), "最近一段经历") + "”中最有代表性的成果是什么？当时遇到的难点是什么？");
            questions.add("如果重新做这段经历里的一个项目，你会优先优化哪个环节？为什么？");
        }
        if (skill != null && hasText(skill.getKeywords())) {
            questions.add("请挑一个你最熟悉的技能关键词，说明它在真实项目中的使用场景和经验。");
        }
        questions.add("你希望下一份工作重点提升哪项能力？你准备如何衡量提升效果？");
        return questions;
    }

    private Set<String> extractJdKeywords(String jdContent) {
        Set<String> result = new LinkedHashSet<>();
        String lower = jdContent.toLowerCase(Locale.ROOT);
        for (String keyword : COMMON_JD_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                result.add(keyword);
            }
        }
        for (String word : splitWords(jdContent)) {
            if (word.length() >= 2 && word.length() <= 24 && result.size() < 24) {
                result.add(word);
            }
        }
        return result;
    }

    private Set<String> extractResumeKeywords(ResumeSnapshot snapshot) {
        Set<String> result = new LinkedHashSet<>();
        if (snapshot.profile() != null) {
            addWords(result, snapshot.profile().getDescription());
            addWords(result, snapshot.profile().getCity());
        }
        if (snapshot.skill() != null) {
            addWords(result, snapshot.skill().getKeywords());
        }
        snapshot.works().forEach(work -> {
            addWords(result, work.getCompany());
            addWords(result, work.getJob());
            addWords(result, work.getDescription());
        });
        return result;
    }

    private void addWords(Set<String> target, String content) {
        splitWords(content).stream().filter(word -> word.length() >= 2 && word.length() <= 30).forEach(target::add);
    }

    private List<String> splitWords(String content) {
        if (!StringUtils.hasText(content)) {
            return List.of();
        }
        List<String> words = new ArrayList<>();
        for (String word : content.split("[,，、。；;：:\\s/\\-]+")) {
            if (StringUtils.hasText(word)) {
                words.add(word.trim());
            }
        }
        return words;
    }

    private boolean containsIgnoreCase(Set<String> source, String keyword) {
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return source.stream().anyMatch(item -> item.toLowerCase(Locale.ROOT).contains(lowerKeyword)
                || lowerKeyword.contains(item.toLowerCase(Locale.ROOT)));
    }

    private String polishWork(Work work) {
        String company = label(work.getCompany(), "相关项目");
        String job = label(work.getJob(), "核心成员");
        String desc = StrUtil.blankToDefault(work.getDescription(), "围绕业务目标完成关键任务");
        return "在" + company + "担任" + job + "，围绕业务目标推进关键任务；建议将原描述“"
                + StrUtil.maxLength(desc, 45) + "”改写为“负责具体行动 + 使用方法/工具 + 量化结果”的表达。";
    }

    private String localPolish(String scene, String content) {
        String normalizedScene = StrUtil.blankToDefault(scene, "简历内容");
        String cleaned = content.trim().replaceAll("\\s+", " ");
        if (cleaned.length() > 160) {
            cleaned = StrUtil.maxLength(cleaned, 160);
        }
        return "【" + normalizedScene + "】" + cleaned
                + "\n优化建议：建议补充“负责事项 + 使用方法/工具 + 量化结果”，例如效率提升、用户增长、交付周期、成本下降等具体成果。";
    }

    private void recordUsage(Long userId, String actionType, boolean success, boolean fallbackUsed, long costMs, String errorMsg) {
        try {
            aiUsageLogService.save(AiUsageLog.builder()
                    .userId(userId)
                    .actionType(actionType)
                    .provider(systemConfigService.getValue("ai.provider", "local"))
                    .model(systemConfigService.getValue("ai.model", "local-rule"))
                    .success(success ? 1 : 0)
                    .fallbackUsed(fallbackUsed ? 1 : 0)
                    .costMs(costMs)
                    .errorMsg(errorMsg)
                    .build());
        } catch (Exception ignored) {
            // 使用日志不能影响 AI 主流程。
        }
    }

    private List<Work> sortedWorks(List<Work> works) {
        if (works == null) {
            return new ArrayList<>();
        }
        return works.stream().sorted(Comparator.comparing(Work::getPriority, Comparator.nullsLast(Integer::compareTo))).toList();
    }

    private AiResumeAnalysisVO.SectionScore section(String name, Integer score) {
        return AiResumeAnalysisVO.SectionScore.builder().name(name).score(score).comment(comment(score)).build();
    }

    private String summary(Profiles profile, int score, List<Work> works) {
        String name = profile == null || !hasText(profile.getName()) ? "这份简历" : profile.getName() + "的简历";
        return name + "当前评分为 " + score + " 分，已具备基础结构"
                + (works == null || works.isEmpty() ? "，但还需要补充经历内容。" : "，下一步重点是强化经历中的量化成果。");
    }

    private String level(int score) {
        if (score >= 85) return "优秀";
        if (score >= 70) return "良好";
        if (score >= 55) return "可优化";
        return "待完善";
    }

    private String matchLevel(int score) {
        if (score >= 85) return "高度匹配";
        if (score >= 70) return "较匹配";
        if (score >= 55) return "需要定向优化";
        return "匹配度偏低";
    }

    private String comment(int score) {
        if (score >= 85) return "内容较完整，可以继续打磨表达。";
        if (score >= 70) return "基础不错，建议补充更多结果数据。";
        if (score >= 55) return "有基本信息，但说服力还可以加强。";
        return "信息偏少，建议优先补齐。";
    }

    private int priorityWeight(String priority) {
        return switch (String.valueOf(priority)) {
            case "HIGH" -> 1;
            case "MEDIUM" -> 2;
            default -> 3;
        };
    }

    private int skillCount(Skill skill) {
        return skill == null || !hasText(skill.getKeywords()) ? 0 : splitWords(skill.getKeywords()).size();
    }

    private boolean hasText(String value) {
        return StrUtil.isNotBlank(value);
    }

    private int missing(List<String> problems, String message) {
        problems.add(message);
        return 0;
    }

    private int cap(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String label(String value, String fallback) {
        return StrUtil.blankToDefault(value, fallback);
    }

    private boolean containsMetric(String text) {
        return text != null && text.matches(".*\\d+.*");
    }

    private List<String> defaultList(List<String> list, String fallback) {
        return list == null || list.isEmpty() ? List.of(fallback) : list;
    }

    private record ResumeSnapshot(Profiles profile, List<Work> works, List<Edu> edus, Skill skill, List<Specialty> specialties) {
    }
}
