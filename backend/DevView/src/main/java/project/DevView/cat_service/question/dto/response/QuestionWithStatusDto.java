package project.DevView.cat_service.question.dto.response;

import java.util.List;

public record QuestionWithStatusDto(
    Long id,
    String question,
    String answer,
    String field,
    boolean answered
) {
} 