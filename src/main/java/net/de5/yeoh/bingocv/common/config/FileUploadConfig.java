package net.de5.yeoh.bingocv.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置
 * 
 * 配置说明：
 * - upload.path: 文件存储根路径，默认为 ./uploads
 * - upload.avatar-path: 头像存储子路径，默认为 /avatars
 * - upload.allowed-types: 允许的文件类型，默认为 jpg,jpeg,png,gif,webp
 */
@Configuration
public class FileUploadConfig {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${upload.avatar-path:/avatars}")
    private String avatarPath;

    @Value("${upload.allowed-types:jpg,jpeg,png,gif,webp}")
    private String allowedTypes;

    public String getUploadPath() {
        return uploadPath;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public String getAvatarFullPath() {
        return uploadPath + avatarPath;
    }

    public String[] getAllowedTypes() {
        return allowedTypes.split(",");
    }

    public boolean isAllowedType(String extension) {
        if (extension == null) {
            return false;
        }
        String ext = extension.toLowerCase();
        for (String allowed : getAllowedTypes()) {
            if (allowed.trim().equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }
}
