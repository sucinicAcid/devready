package project.DevView.cat_service.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import project.DevView.cat_service.interview.entity.Interview;

import java.time.LocalDateTime;

@Builder
public record InterviewResponseDto(
        @NotNull
        @Schema(description = "인터뷰 ID", example = "1")
        Long interviewId,

        @NotNull
        @Schema(description = "사용자 ID", example = "42")
        Long userId,

        @NotNull
        @Schema(description = "인터뷰 분야 이름", example = "OS")
        String fieldName,

        @Schema(description = "인터뷰 시작 시각")
        LocalDateTime startedAt,

        @Schema(description = "인터뷰 종료 시각")
        LocalDateTime endedAt
) {
    public static InterviewResponseDto of(Interview interview) {
        return InterviewResponseDto.builder()
                .interviewId(interview.getId())
                .userId(interview.getUser().getId())
                .fieldName(interview.getField().toString())
                .startedAt(interview.getStartedAt())
                .endedAt(interview.getEndedAt())
                .build();
    }
}
