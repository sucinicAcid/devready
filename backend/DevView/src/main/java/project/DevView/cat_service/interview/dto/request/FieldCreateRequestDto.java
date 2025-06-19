package project.DevView.cat_service.interview.dto.request;

import lombok.Getter;

/**
 * Field 생성 시 클라이언트에서 보낼 Request DTO
 */
@Getter
public class FieldCreateRequestDto {
    private final String name;

    public FieldCreateRequestDto(String name) {
        this.name = name;
    }
}
