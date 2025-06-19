package project.DevView.cat_service.question.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import project.DevView.cat_service.question.entity.Question;

@Builder
public record QuestionResponseDto(
        @NotNull
        @Schema(description = "질문 id", example = "1")
        Long questionId,

        @NotNull
        @Schema(description = "질문 내용", example = "인터뷰 질문 예시입니다")
        String question,

        @NotNull
        @Schema(description = "질문 내용", example = "인터뷰 질문 예시입니다")
        String answer,

        @NotNull
        @Schema(description = "연결된 Field 이름", example = "OS")
        String field
) {
    public static QuestionResponseDto of(Question question) {
        return QuestionResponseDto.builder()
                .questionId(question.getId())
                .question(question.getQuestion())
                .answer(question.getAnswer())
                .field(question.getField().toString())
                .build();
    }
}
