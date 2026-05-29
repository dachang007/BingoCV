package net.de5.yeoh.bingocv.model.vo;

import lombok.Builder;
import lombok.Data;
import net.de5.yeoh.bingocv.model.domain.*;

import java.util.List;

@Data
@Builder
public class PublicResumeVO {
    private ShareVO share;
    private Profiles profile;
    private List<Edu> educationList;
    private List<Work> workList;
    private Skill skill;
    private List<Specialty> specialtyList;
}
