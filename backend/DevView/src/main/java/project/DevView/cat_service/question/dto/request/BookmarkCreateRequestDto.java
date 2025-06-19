package project.DevView.cat_service.question.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BookmarkCreateRequestDto(
        @NotNull
        @Schema(description = "북마크할 메시지 ID", example = "123")
        Long messageId
) {}
