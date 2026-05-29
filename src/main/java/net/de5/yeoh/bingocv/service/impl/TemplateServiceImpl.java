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
import net.de5.yeoh.bingocv.service.UserTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 模板服务实现类
 * 提供模板市场浏览、我的模板管理、模板获取和激活等功能
 */
@Service
public class TemplateServiceImpl extends ServiceImpl<TemplateMapper, Template> implements TemplateService {

    @Autowired
    private UserTemplateService userTemplateService;
    
    @Autowired
    private PointsService pointsService;

    /**
     * 浏览模板市场
     * 根据行业分类和模板类型筛选模板，并标记用户已拥有和正在使用的模板
     *
     * @param industry 行业分类（如：IT互联网、金融财会等），"全部"表示不筛选
     * @param type 模板类型（FREE/POINTS/PAID），"ALL"表示不筛选
     * @param userId 用户ID，用于标记用户已拥有的模板
     * @return 模板列表，包含 owned（是否拥有）和 active（是否启用）字段
     */
    @Override
    public List<Template> market(String industry, String type, Long userId) {
        // 构建查询条件：只查询上架的模板，按排序值降序、ID升序排列
        LambdaQueryWrapper<Template> wrapper = new LambdaQueryWrapper<Template>()
                .eq(Template::getStatus, 1)
                .orderByDesc(Template::getSortOrder)
                .orderByAsc(Template::getId);
        
        // 如果指定了行业且不是"全部"，则添加行业筛选条件
        if (StringUtils.hasText(industry) && !"全部".equals(industry)) {
            wrapper.eq(Template::getIndustry, industry);
        }
        
        // 如果指定了类型且不是"ALL"，则添加类型筛选条件
        if (StringUtils.hasText(type) && !"ALL".equalsIgnoreCase(type)) {
            wrapper.eq(Template::getTemplateType, type.toUpperCase());
        }

        // 查询符合条件的模板列表
        List<Template> templates = this.list(wrapper);
        
        // 如果没有用户ID或模板列表为空，直接返回
        if (userId == null || templates.isEmpty()) {
            return templates;
        }
        
        // 查询用户已拥有的所有模板，构建 Map<模板ID, 用户模板关系>
        Map<Long, UserTemplate> ownedMap = userTemplateService.list(new LambdaQueryWrapper<UserTemplate>()
                        .eq(UserTemplate::getUserId, userId))
                .stream()
                .collect(Collectors.toMap(UserTemplate::getTemplateId, Function.identity(), (a, b) -> a));
        
        // 为每个模板标记是否拥有和是否启用
        templates.forEach(template -> {
            UserTemplate userTemplate = ownedMap.get(template.getId());
            template.setOwned(userTemplate != null);  // 标记是否已拥有
            template.setActive(userTemplate != null && Integer.valueOf(1).equals(userTemplate.getActive()));  // 标记是否已启用
        });
        return templates;
    }

    /**
     * 获取用户的模板列表（我的模板）
     * 查询用户拥有的所有模板，并关联模板详情
     *
     * @param userId 用户ID
     * @return 用户模板列表，按启用状态降序、创建时间降序排列
     */
    @Override
    public List<UserTemplate> myTemplates(Long userId) {
        // 查询用户拥有的所有模板关系，启用的排在前面
        List<UserTemplate> list = userTemplateService.list(new LambdaQueryWrapper<UserTemplate>()
                .eq(UserTemplate::getUserId, userId)
                .orderByDesc(UserTemplate::getActive)
                .orderByDesc(UserTemplate::getCreateTime));
        
        // 为每个用户模板关系加载对应的模板详情
        list.forEach(item -> item.setTemplate(this.getById(item.getTemplateId())));
        return list;
    }

    /**
     * 获取/兑换模板
     * 如果用户未拥有该模板，则根据模板类型进行免费获取或积分兑换
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     * @return 用户模板关系对象
     * @throws InfoException 当模板不存在、已下架或付费模板未接入支付时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserTemplate acquire(Long userId, Long templateId) {
        // 1. 查询模板信息，验证模板是否存在且已上架
        Template template = this.getById(templateId);
        if (template == null || !Integer.valueOf(1).equals(template.getStatus())) {
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "模板不存在或已下架");
        }

        // 2. 检查用户是否已拥有该模板，如果已拥有则直接返回
        UserTemplate owned = userTemplateService.getOne(new LambdaQueryWrapper<UserTemplate>()
                .eq(UserTemplate::getUserId, userId)
                .eq(UserTemplate::getTemplateId, templateId));
        if (owned != null) {
            return owned;
        }

        // 3. 根据模板类型进行处理
        String source = template.getTemplateType();
        if ("POINTS".equals(template.getTemplateType())) {
            // 积分兑换：扣除相应积分
            int cost = template.getPointsCost() == null ? 0 : template.getPointsCost();
            pointsService.change(
                    userId, -cost, "TEMPLATE_EXCHANGE",
                    templateId, "兑换模板：" + template.getName());
        } else if ("PAID".equals(template.getTemplateType())) {
            // TODO：接入支付
            throw new InfoException(InfoEnum.PARAMS_ERROR.getCode(), "付费模板暂未接入支付");
        }
        // FREE 类型模板：直接获取，无需扣费

        // 4. 创建用户模板关系记录，默认不启用
        UserTemplate userTemplate = UserTemplate.builder()
                .userId(userId)
                .templateId(templateId)
                .source(source)  // 记录来源：FREE/POINTS/PAID
                .active(0)       // 默认不启用
                .build();
        userTemplateService.save(userTemplate);
        return userTemplate;
    }

    /**
     * 激活/切换模板
     * 核心逻辑：先将用户所有模板设为未启用，再将指定模板设为启用
     * 这是一个原子操作，保证同一时刻只有一个模板处于启用状态
     *
     * @param userId 用户ID
     * @param templateId 要启用的模板ID
     * @return 更新后的用户模板关系对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserTemplate activate(Long userId, Long templateId) {
        // 1. 确保用户拥有该模板（如果未拥有则先获取/兑换）
        UserTemplate userTemplate = acquire(userId, templateId);
        
        // 2. 【关键步骤】将用户的所有模板都设为未启用状态（active=0）
        //    这确保了同一时刻只有一个模板处于启用状态
        userTemplateService.update(new LambdaUpdateWrapper<UserTemplate>()
                .eq(UserTemplate::getUserId, userId)
                .set(UserTemplate::getActive, 0));
        
        // 3. 将指定模板设为启用状态（active=1）
        userTemplate.setActive(1);
        userTemplateService.updateById(userTemplate);
        
        return userTemplate;
    }
}
