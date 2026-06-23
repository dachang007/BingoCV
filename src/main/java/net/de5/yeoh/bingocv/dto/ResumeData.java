package net.de5.yeoh.bingocv.dto;

import lombok.Data;
import net.de5.yeoh.bingocv.model.domain.*;

import java.util.List;

@Data
public class ResumeData {
    
    private Profiles profile;
    private List<Work> work;
    private List<Edu> edu;
    private Skill skill;
    private List<Specialty> specialty;
}
