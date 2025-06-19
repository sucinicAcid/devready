package project.DevView.cat_service.question.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FieldCreateRequestDto(
        @NotBlank(message = "Field 이름은 필수입니다.")
        @Size(min = 1, max = 50, message = "Field 이름은 1~50자 사이여야 합니다.")
        String name
) {
}
