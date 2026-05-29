package net.de5.yeoh.bingocv.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.de5.yeoh.bingocv.model.domain.Template;
import net.de5.yeoh.bingocv.model.domain.UserTemplate;

import java.util.List;

public interface TemplateService extends IService<Template> {
    List<Template> market(String industry, String type, Long userId);

    List<UserTemplate> myTemplates(Long userId);

    UserTemplate acquire(Long userId, Long templateId);

    UserTemplate activate(Long userId, Long templateId);
}
