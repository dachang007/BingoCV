package net.de5.yeoh.bingocv.service.impl;

import cn.hutool.core.util.StrUtil;
import com.lowagie.text.DocumentException;
import net.de5.yeoh.bingocv.dto.ResumeData;
import net.de5.yeoh.bingocv.model.domain.Edu;
import net.de5.yeoh.bingocv.model.domain.Profiles;
import net.de5.yeoh.bingocv.model.domain.Skill;
import net.de5.yeoh.bingocv.model.domain.Specialty;
import net.de5.yeoh.bingocv.model.domain.Work;
import net.de5.yeoh.bingocv.service.ResumeExportService;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ResumeExportServiceImpl implements ResumeExportService {

    private static final String DEFAULT_TEMPLATE = "general-minimal";

    private final TemplateEngine templateEngine;

    public ResumeExportServiceImpl() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setPrefix("templates/export/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
    }

    @Override
    public ByteArrayOutputStream exportToPdf(ResumeData resumeData, String templateKey) {
        String html = generateHtml(resumeData, templateKey);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            renderer.finishPDF();
        } catch (DocumentException e) {
            throw new RuntimeException("PDF 导出失败", e);
        }
        return outputStream;
    }

    @Override
    public ByteArrayOutputStream exportToWord(ResumeData resumeData, String templateKey) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (XWPFDocument document = new XWPFDocument()) {
            Profiles profile = resumeData.getProfile();
            if (profile != null) {
                XWPFParagraph titleParagraph = document.createParagraph();
                titleParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun titleRun = titleParagraph.createRun();
                titleRun.setText(StrUtil.blankToDefault(profile.getName(), "BingoCV 简历"));
                titleRun.setBold(true);
                titleRun.setFontSize(24);

                addInfoParagraph(document, "联系方式",
                        StrUtil.format("{} | {} | {}",
                                StrUtil.blankToDefault(profile.getPhone(), ""),
                                StrUtil.blankToDefault(profile.getEmail(), ""),
                                StrUtil.blankToDefault(profile.getCity(), "")));

                if (StrUtil.isNotBlank(profile.getDescription())) {
                    addSectionTitle(document, "个人简介");
                    addContentParagraph(document, profile.getDescription());
                }
            }

            List<Work> workList = resumeData.getWork();
            if (workList != null && !workList.isEmpty()) {
                addSectionTitle(document, "工作经历");
                workList.forEach(work -> addWorkExperience(document, work));
            }

            List<Edu> eduList = resumeData.getEdu();
            if (eduList != null && !eduList.isEmpty()) {
                addSectionTitle(document, "教育经历");
                eduList.forEach(edu -> addEducation(document, edu));
            }

            Skill skill = resumeData.getSkill();
            if (skill != null && StrUtil.isNotBlank(skill.getKeywords())) {
                addSectionTitle(document, "专业技能");
                addContentParagraph(document, skill.getKeywords());
            }

            List<Specialty> specialtyList = resumeData.getSpecialty();
            if (specialtyList != null && !specialtyList.isEmpty()) {
                addSectionTitle(document, "个人特长");
                for (Specialty specialty : specialtyList) {
                    String text = StrUtil.blankToDefault(specialty.getName(), "");
                    if (StrUtil.isNotBlank(specialty.getDescription())) {
                        text += "：" + specialty.getDescription();
                    }
                    addContentParagraph(document, text);
                }
            }

            document.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Word 导出失败", e);
        }
        return outputStream;
    }

    @Override
    public ByteArrayOutputStream exportToMarkdown(ResumeData resumeData, String templateKey) {
        StringBuilder md = new StringBuilder();
        Profiles profile = resumeData.getProfile();
        if (profile != null) {
            md.append("# ").append(StrUtil.blankToDefault(profile.getName(), "BingoCV 简历")).append("\n\n");
            String contact = StrUtil.format("{} | {} | {}",
                    StrUtil.blankToDefault(profile.getPhone(), ""),
                    StrUtil.blankToDefault(profile.getEmail(), ""),
                    StrUtil.blankToDefault(profile.getCity(), ""));
            if (StrUtil.isNotBlank(contact.replace(" | ", "").trim())) {
                md.append("**联系方式：** ").append(contact).append("\n\n");
            }
            if (StrUtil.isNotBlank(profile.getDescription())) {
                md.append("## 个人简介\n\n").append(profile.getDescription()).append("\n\n");
            }
        }

        List<Work> workList = resumeData.getWork();
        if (workList != null && !workList.isEmpty()) {
            md.append("## 工作经历\n\n");
            for (Work work : workList) {
                md.append("### ").append(StrUtil.blankToDefault(work.getCompany(), "未填写公司")).append("\n\n");
                md.append("**职位：** ").append(StrUtil.blankToDefault(work.getJob(), "")).append("\n\n");
                md.append("**时间：** ").append(StrUtil.blankToDefault(work.getStart(), ""))
                        .append(" - ").append(StrUtil.blankToDefault(work.getEnd(), "至今")).append("\n\n");
                if (StrUtil.isNotBlank(work.getDescription())) {
                    md.append(work.getDescription()).append("\n\n");
                }
            }
        }

        List<Edu> eduList = resumeData.getEdu();
        if (eduList != null && !eduList.isEmpty()) {
            md.append("## 教育经历\n\n");
            for (Edu edu : eduList) {
                md.append("- **").append(StrUtil.blankToDefault(edu.getSchool(), "未填写学校")).append("** - ")
                        .append(StrUtil.blankToDefault(edu.getStudy(), ""))
                        .append(" (").append(StrUtil.blankToDefault(edu.getStart(), ""))
                        .append(" - ").append(StrUtil.blankToDefault(edu.getEnd(), "")).append(")\n");
            }
            md.append("\n");
        }

        Skill skill = resumeData.getSkill();
        if (skill != null && StrUtil.isNotBlank(skill.getKeywords())) {
            md.append("## 专业技能\n\n").append(skill.getKeywords()).append("\n\n");
        }

        List<Specialty> specialtyList = resumeData.getSpecialty();
        if (specialtyList != null && !specialtyList.isEmpty()) {
            md.append("## 个人特长\n\n");
            for (Specialty specialty : specialtyList) {
                md.append("- **").append(StrUtil.blankToDefault(specialty.getName(), "")).append("**");
                if (StrUtil.isNotBlank(specialty.getDescription())) {
                    md.append("：").append(specialty.getDescription());
                }
                md.append("\n");
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(md.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Markdown 导出失败", e);
        }
        return outputStream;
    }

    private String generateHtml(ResumeData resumeData, String templateKey) {
        Context context = new Context();
        context.setVariable("resume", resumeData);

        // 后端当前只有通用导出模板，其他前端模板先回退到通用导出版。
        String templateName = templateExists(templateKey) ? templateKey : DEFAULT_TEMPLATE;
        return templateEngine.process(templateName, context);
    }

    private boolean templateExists(String templateKey) {
        if (StrUtil.isBlank(templateKey)) {
            return false;
        }
        return new ClassPathResource("templates/export/" + templateKey + ".html").exists();
    }

    private void addSectionTitle(XWPFDocument document, String title) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBefore(200);
        XWPFRun run = paragraph.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(14);
    }

    private void addInfoParagraph(XWPFDocument document, String label, String content) {
        if (StrUtil.isBlank(content.replace(" | ", "").trim())) {
            return;
        }
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(label + "：" + content);
        run.setFontSize(11);
    }

    private void addContentParagraph(XWPFDocument document, String content) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(content);
        run.setFontSize(11);
    }

    private void addWorkExperience(XWPFDocument document, Work work) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(StrUtil.format("{} - {}",
                StrUtil.blankToDefault(work.getCompany(), ""),
                StrUtil.blankToDefault(work.getJob(), "")));
        run.setBold(true);
        run.setFontSize(12);

        paragraph = document.createParagraph();
        run = paragraph.createRun();
        run.setText(StrUtil.format("{} - {}",
                StrUtil.blankToDefault(work.getStart(), ""),
                StrUtil.blankToDefault(work.getEnd(), "至今")));
        run.setFontSize(10);
        run.setItalic(true);

        if (StrUtil.isNotBlank(work.getDescription())) {
            addContentParagraph(document, work.getDescription());
        }
    }

    private void addEducation(XWPFDocument document, Edu edu) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(StrUtil.format("{} - {}",
                StrUtil.blankToDefault(edu.getSchool(), ""),
                StrUtil.blankToDefault(edu.getStudy(), "")));
        run.setFontSize(11);

        paragraph = document.createParagraph();
        run = paragraph.createRun();
        run.setText(StrUtil.format("{} - {}",
                StrUtil.blankToDefault(edu.getStart(), ""),
                StrUtil.blankToDefault(edu.getEnd(), "")));
        run.setFontSize(10);
        run.setItalic(true);
    }
}
