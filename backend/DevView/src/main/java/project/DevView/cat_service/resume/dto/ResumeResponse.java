package project.DevView.cat_service.resume.dto;

import lombok.Builder;
import lombok.Getter;
import project.DevView.cat_service.resume.entity.Resume;
import project.DevView.cat_service.resume.entity.ResumeTag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ResumeResponse {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ResumeTagResponse> tags;

    public static ResumeResponse from(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .userId(resume.getUser().getId())
                .content(resume.getContent())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .tags(resume.getTags().stream()
                        .map(tag -> ResumeTagResponse.builder()
                                .keywords(List.of(ResumeTagResponse.KeywordItem.builder()
                                        .tagType(tag.getTagType())
                                        .keyword(tag.getKeyword())
                                        .detail(tag.getDetail())
                                        .depthScore(tag.getDepthScore())
                                        .priorityScore(tag.getPriorityScore())
                                        .build()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
} 