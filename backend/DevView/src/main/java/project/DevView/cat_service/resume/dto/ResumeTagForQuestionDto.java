package project.DevView.cat_service.resume.dto;

import lombok.Builder;
import lombok.Getter;
import project.DevView.cat_service.resume.entity.ResumeTag;
import project.DevView.cat_service.resume.entity.TagType;

@Getter
@Builder
public class ResumeTagForQuestionDto {
    private TagType tagType;
    private String keyword;
    private String detail;
    private int depthScore;
    private int priorityScore;

    public static ResumeTagForQuestionDto from(ResumeTag tag) {
        return ResumeTagForQuestionDto.builder()
                .tagType(tag.getTagType())
                .keyword(tag.getKeyword())
                .detail(tag.getDetail())
                .depthScore(tag.getDepthScore())
                .priorityScore(tag.getPriorityScore())
                .build();
    }
} 