package project.DevView.cat_service.interview.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record InterviewCreateRequestDto(
        @NotBlank
        @Schema(description = "인터뷰 분야 이름", example = "OS")
        String field,

        @Schema(description = "이력서 ID", example = "1")
        Long resumeId
) {}
