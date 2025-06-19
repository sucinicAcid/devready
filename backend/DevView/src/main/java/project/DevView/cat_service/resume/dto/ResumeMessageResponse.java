package project.DevView.cat_service.resume.dto;

import lombok.Builder;
import lombok.Getter;
import project.DevView.cat_service.resume.entity.ResumeMessage;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResumeMessageResponse {
    private Long id;
    private Long resumeId;
    private ResumeMessage.MessageType type;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ResumeMessageResponse from(ResumeMessage message) {
        return ResumeMessageResponse.builder()
                .id(message.getId())
                .resumeId(message.getResume().getId())
                .type(message.getType())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
} 