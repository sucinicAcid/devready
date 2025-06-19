package project.DevView.cat_service.question.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record QuestionCreateRequest(
        @NotBlank
        @Schema(description = "질문 내용", example = "인터뷰 질문 예시입니다")
        String question,

        @NotBlank
        @Schema(description = "답변 내용", example = "인터뷰 질문 답변 예시입니다")
        String answer,

        @NotBlank
        @Schema(description = "필드 명", example="OS")
        String field
) {}
