package project.DevView.cat_service.resume.dto;

import lombok.Builder;
import lombok.Getter;
import project.DevView.cat_service.resume.entity.TagType;

import java.util.List;

@Getter
@Builder
public class ResumeTagResponse {
    private List<KeywordItem> keywords;
    private List<String> advancedPoints;
    private List<String> detectedGaps;

    @Getter
    @Builder
    public static class KeywordItem {
        private TagType tagType;
        private String keyword;
        private String detail;
        private int depthScore;
        private int priorityScore;
    }
} 