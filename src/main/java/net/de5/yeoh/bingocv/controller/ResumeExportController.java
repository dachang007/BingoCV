package net.de5.yeoh.bingocv.controller;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.dto.ExportResumeDTO;
import net.de5.yeoh.bingocv.dto.ResumeData;
import net.de5.yeoh.bingocv.model.domain.*;
import net.de5.yeoh.bingocv.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/export")
@Tag(name = "简历导出接口")
public class ResumeExportController {

    @Autowired
    private ResumeExportService resumeExportService;
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

    @PostMapping("/resume")
    @CheckLogin
    @Operation(summary = "导出当前用户简历")
    public void exportMyResume(@RequestBody @Valid ExportResumeDTO dto, HttpServletResponse response) throws IOException {
        exportResume(UserContext.currentUserId(), dto, response);
    }

    @PostMapping("/resume/user")
    @CheckLogin
    @Operation(summary = "导出指定用户简历")
    public void exportUserResume(@RequestBody @Valid ExportResumeDTO dto, HttpServletResponse response) throws IOException {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        exportResume(dto.getUserId(), dto, response);
    }

    private void exportResume(Long userId, ExportResumeDTO dto, HttpServletResponse response) throws IOException {
        ResumeData resumeData = buildResumeData(userId);
        String format = StrUtil.blankToDefault(dto.getFormat(), "pdf").toLowerCase();
        String templateKey = StrUtil.blankToDefault(dto.getTemplateKey(), "general-minimal");

        ByteArrayOutputStream outputStream;
        String contentType;
        String extension;

        switch (format) {
            case "word":
                outputStream = resumeExportService.exportToWord(resumeData, templateKey);
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                extension = "docx";
                break;
            case "markdown":
                outputStream = resumeExportService.exportToMarkdown(resumeData, templateKey);
                contentType = "text/markdown;charset=UTF-8";
                extension = "md";
                break;
            default:
                outputStream = resumeExportService.exportToPdf(resumeData, templateKey);
                contentType = "application/pdf";
                extension = "pdf";
                break;
        }

        String fileName = getFileName(resumeData, extension);
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        response.setContentLength(outputStream.size());
        outputStream.writeTo(response.getOutputStream());
        response.getOutputStream().flush();
    }

    private ResumeData buildResumeData(Long userId) {
        ResumeData resumeData = new ResumeData();
        resumeData.setProfile(profilesService.getByUserId(userId));

        List<Work> workList = workService.listByUserId(userId);
        resumeData.setWork(workList);

        List<Edu> eduList = eduService.listByUserId(userId);
        resumeData.setEdu(eduList);

        resumeData.setSkill(skillService.getByUserId(userId));
        resumeData.setSpecialty(specialtyService.listByUserId(userId));
        return resumeData;
    }

    private String getFileName(ResumeData resumeData, String extension) {
        String name = "BingoCV简历";
        if (resumeData.getProfile() != null && StrUtil.isNotBlank(resumeData.getProfile().getName())) {
            name = resumeData.getProfile().getName() + "_简历";
        }
        return URLEncoder.encode(name + "." + extension, StandardCharsets.UTF_8);
    }
}
