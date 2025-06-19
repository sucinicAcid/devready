package project.DevView.cat_service.question.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserQuestionHistoryCreateRequestDto(
        @NotNull
        @Schema(description = "질문 ID", example = "456")
        Long questionId,

        @NotNull
        @Schema(description = "완료 여부", example = "true")
        Boolean completed
) {}
