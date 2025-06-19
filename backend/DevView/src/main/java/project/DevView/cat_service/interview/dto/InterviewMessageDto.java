package project.DevView.cat_service.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import project.DevView.cat_service.interview.entity.InterviewMessage;

import java.time.LocalDateTime;

@Schema(description = "인터뷰 메시지 DTO")
public record InterviewMessageDto(
    @Schema(description = "메시지 ID", example = "1")
    Long id,
    
    @Schema(description = "발신자", example = "SYSTEM")
    String sender,
    
    @Schema(description = "메시지 타입", example = "QUESTION")
    String messageType,
    
    @Schema(description = "메시지 내용", example = "운영체제란 무엇인가요?")
    String content,
    
    @Schema(description = "생성 시간", example = "2024-02-22T03:45:24")
    LocalDateTime createdAt
) {
    public static InterviewMessageDto from(InterviewMessage message) {
        return new InterviewMessageDto(
            message.getId(),
            message.getSender(),
            message.getMessageType(),
            message.getContent(),
            message.getCreatedAt()
        );
    }
} 