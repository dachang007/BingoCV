package net.de5.yeoh.bingocv.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import net.de5.yeoh.bingocv.common.config.FileUploadConfig;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new InfoException(InfoEnum.PARAM_IS_EMPTY, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "文件名不能为空");
        }

        String extension = FileUtil.extName(originalFilename).toLowerCase();
        if (!fileUploadConfig.isAllowedType(extension)) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(),
                    "不支持的文件类型，仅支持: " + String.join(", ", fileUploadConfig.getAllowedTypes()));
        }

        String avatarDir = fileUploadConfig.getAvatarFullPath();
        try {
            Path dirPath = Paths.get(avatarDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("创建头像存储目录: {}", avatarDir);
            }
        } catch (IOException e) {
            log.error("创建头像存储目录失败: {}", avatarDir, e);
            throw new InfoException(InfoEnum.SYSTEM_ERROR.getCode(), "创建存储目录失败");
        }

        String newFilename = userId + "_" + IdUtil.fastSimpleUUID() + "." + extension;
        Path filePath = Paths.get(avatarDir, newFilename);

        try {
            file.transferTo(filePath.toFile());
            log.info("头像上传成功: {}", filePath);
        } catch (IOException e) {
            log.error("头像上传失败: {}", filePath, e);
            throw new InfoException(InfoEnum.SYSTEM_ERROR.getCode(), "文件保存失败");
        }

        return "/uploads" + fileUploadConfig.getAvatarPath() + "/" + newFilename;
    }

    @Override
    public boolean deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return false;
        }

        try {
            String fullPath = fileUploadConfig.getUploadPath().replace("./", "")
                    + relativePath.replace("/uploads", "");
            File file = new File(fullPath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功: {}", fullPath);
                }
                return deleted;
            }
            return false;
        } catch (Exception e) {
            log.error("文件删除失败: {}", relativePath, e);
            return false;
        }
    }
}
