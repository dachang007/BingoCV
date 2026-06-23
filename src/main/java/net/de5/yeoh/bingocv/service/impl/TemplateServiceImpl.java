package net.de5.yeoh.bingocv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.de5.yeoh.bingocv.common.enums.InfoEnum;
import net.de5.yeoh.bingocv.common.exception.InfoException;
import net.de5.yeoh.bingocv.mapper.TemplateMapper;
import net.de5.yeoh.bingocv.model.domain.Template;
import net.de5.yeoh.bingocv.model.domain.UserTemplate;
import net.de5.yeoh.bingocv.service.PointsService;
import net.de5.yeoh.bingocv.service.TemplateService;
import net.de5.yeoh.bingocv.service.TaskCompletionService;
import net.de5.yeoh.bingocv.service.UserTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TemplateServiceImpl extends ServiceImpl<TemplateMapper, Template> implements TemplateService {

    @Autowired
    private UserTemplateService userTemplateService;
    @Autowired
    private PointsService pointsService;
    @Autowired
    private TaskCompletionService taskCompletionService;

    @Override
    public List<Template> market(String industry, String type, Long userId) {
        LambdaQueryWrapper<Template> wrapper = new LambdaQueryWrapper<Template>()
                .eq(Template::getStatus, 1)
                .orderByDesc(Template::getSortOrder)
                .orderByAsc(Template::getId);

        if (StringUtils.hasText(industry) && !"全部".equals(industry)) {
            wrapper.eq(Template::getIndustry, industry);
        }
        if (StringUtils.hasText(type) && !"ALL".equalsIgnoreCase(type)) {
            wrapper.eq(Template::getTemplateType, type.toUpperCase());
        }

        List<Template> templates = this.list(wrapper);
        if (userId == null || templates.isEmpty()) {
            return templates;
        }

        Map<Long, UserTemplate> ownedMap = userTemplateService.list(new LambdaQueryWrapper<UserTemplate>()
                        .eq(UserTemplate::getUserId, userId))
                .stream()
                .collect(Collectors.toMap(UserTemplate::getTemplateId, Function.identity(), (a, b) -> a));

        templates.forEach(template -> {
            UserTemplate userTemplate = ownedMap.get(template.getId());
            template.setOwned(userTemplate != null);
            template.setActive(userTemplate != null && Integer.valueOf(1).equals(userTemplate.getActive()));
        });
        return templates;
    }

    @Override
    public List<UserTemplate> myTemplates(Long userId) {
        List<UserTemplate> list = userTemplateService.list(new LambdaQueryWrapper<UserTemplate>()
                .eq(UserTemplate::getUserId, userId)
                .orderByDesc(UserTemplate::getActive)
                .orderByDesc(UserTemplate::getCreateTime));
        list.forEach(item -> item.setTemplate(this.getById(item.getTemplateId())));
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserTemplate acquire(Long userId, Long templateId) {
        Template template = this.getById(templateId);
        if (template == null || !Integer.valueOf(1).equals(template.getStatus())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "模板不存在或已下架");
        }

        UserTemplate owned = userTemplateService.getOne(new LambdaQueryWrapper<UserTemplate>()
                .eq(UserTemplate::getUserId, userId)
                .eq(UserTemplate::getTemplateId, templateId)
                .last("LIMIT 1"));
        if (owned != null) {
            return owned;
        }

        String source = template.getTemplateType();
        if ("POINTS".equals(template.getTemplateType())) {
            int cost = template.getPointsCost() == null ? 0 : template.getPointsCost();
            pointsService.change(userId, -cost, "TEMPLATE_EXCHANGE", templateId, "兑换模板：" + template.getName());
        } else if ("PAID".equals(template.getTemplateType())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "付费模板暂未接入支付");
        }

        UserTemplate userTemplate = UserTemplate.builder()
                .userId(userId)
                .templateId(templateId)
                .source(source)
                .active(0)
                .build();
        userTemplateService.save(userTemplate);
        return userTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserTemplate activate(Long userId, Long templateId) {
        UserTemplate userTemplate = acquire(userId, templateId);
        userTemplateService.update(new LambdaUpdateWrapper<UserTemplate>()
                .eq(UserTemplate::getUserId, userId)
                .set(UserTemplate::getActive, 0));

        userTemplate.setActive(1);
        userTemplateService.updateById(userTemplate);

        // 启用任意模板后即可完成“选择简历模板”新手任务。
        taskCompletionService.completeTask(userId, "select_template");
        return userTemplate;
    }
}
