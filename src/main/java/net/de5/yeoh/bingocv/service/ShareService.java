package net.de5.yeoh.bingocv.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.de5.yeoh.bingocv.model.domain.Share;
import net.de5.yeoh.bingocv.model.dto.ShareAccessRequest;
import net.de5.yeoh.bingocv.model.dto.ShareCreateRequest;
import net.de5.yeoh.bingocv.model.dto.ShareUpdateRequest;
import net.de5.yeoh.bingocv.model.vo.PublicResumeVO;
import net.de5.yeoh.bingocv.model.vo.ShareAccessStatsVO;
import net.de5.yeoh.bingocv.model.vo.ShareVO;

import java.util.List;

public interface ShareService extends IService<Share> {
    List<ShareVO> myShares(Long userId);

    ShareVO createShare(Long userId, ShareCreateRequest request);

    ShareVO updateShare(Long userId, Long id, ShareUpdateRequest request);

    Boolean closeShare(Long userId, Long id);

    ShareAccessStatsVO accessStats(Long userId, Long id);

    PublicResumeVO accessPublicResume(String shortCode, ShareAccessRequest request);

    String redirectTarget(String shortCode);
}
