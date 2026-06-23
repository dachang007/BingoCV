package net.de5.yeoh.bingocv.model.vo;

import lombok.Builder;
import lombok.Data;
import net.de5.yeoh.bingocv.model.domain.ShareAccess;

import java.util.List;

@Data
@Builder
public class ShareAccessStatsVO {
    private Long shareId;
    private Integer accessCount;
    private Long todayCount;
    private Long uniqueVisitorCount;
    private List<ShareAccess> recentRecords;
}
