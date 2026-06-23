package net.de5.yeoh.bingocv.service;

import net.de5.yeoh.bingocv.dto.ResumeData;

import java.io.ByteArrayOutputStream;

public interface ResumeExportService {

    /**
     * 导出为 PDF 格式。
     */
    ByteArrayOutputStream exportToPdf(ResumeData resumeData, String templateKey);

    /**
     * 导出为 Word 格式。
     */
    ByteArrayOutputStream exportToWord(ResumeData resumeData, String templateKey);

    /**
     * 导出为 Markdown 格式。
     */
    ByteArrayOutputStream exportToMarkdown(ResumeData resumeData, String templateKey);
}
