package net.de5.yeoh.bingocv.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    /**
     * 上传头像图片。
     *
     * @param file 图片文件
     * @param userId 用户ID
     * @return 上传后的相对访问路径，例如 /uploads/avatars/123_abc.jpg
     */
    String uploadAvatar(MultipartFile file, Long userId);

    /**
     * 删除已上传文件。
     *
     * @param relativePath 相对路径
     * @return 是否删除成功
     */
    boolean deleteFile(String relativePath);
}
