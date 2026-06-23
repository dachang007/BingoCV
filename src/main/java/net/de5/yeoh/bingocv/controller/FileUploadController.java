package net.de5.yeoh.bingocv.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.de5.yeoh.bingocv.common.annotation.CheckLogin;
import net.de5.yeoh.bingocv.common.api.Result;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.common.utils.mvc.UserContext;
import net.de5.yeoh.bingocv.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@Slf4j
@Tag(name = "文件上传接口")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/avatar")
    @CheckLogin
    @Operation(summary = "上传头像图片")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = requireUserId();
        String filePath = fileUploadService.uploadAvatar(file, userId);
        return Result.ok(fileResult(filePath));
    }

    @PostMapping("/image")
    @CheckLogin
    @Operation(summary = "上传通用图片")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        Long userId = requireUserId();
        String filePath = fileUploadService.uploadAvatar(file, userId);
        return Result.ok(fileResult(filePath));
    }

    private Long requireUserId() {
        Long userId = UserContext.currentUserId();
        if (userId == null) {
            throw new InfoException(InfoEnum.NOT_LOGIN);
        }
        return userId;
    }

    private Map<String, String> fileResult(String filePath) {
        Map<String, String> result = new HashMap<>();
        result.put("path", filePath);
        result.put("url", filePath);
        return result;
    }
}
