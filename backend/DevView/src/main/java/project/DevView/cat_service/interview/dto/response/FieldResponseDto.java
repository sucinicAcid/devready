package project.DevView.cat_service.interview.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Field 조회/생성 결과용 DTO
 */
@Getter
@AllArgsConstructor
public class FieldResponseDto {
    private final Long id;
    private final String name;
}
