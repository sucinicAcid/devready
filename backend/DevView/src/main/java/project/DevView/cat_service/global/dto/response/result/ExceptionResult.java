package project.DevView.cat_service.global.dto.response.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class ExceptionResult {
    @Getter
    @Builder
    public static class ServerErrorData {
        @Schema(description = "오류 발생 클래스", example = "org.example.XX")
        private final String errorClass;

        @Schema(description = "오류 메세지")
        private final String errorMessage;
    }

    //파라미터 에러에서 상세 정보를 얻고 싶을 때
    @Getter
    @Builder
    public static class ParameterData {
        @Schema(description = "오류가 발생한 필드", example = "title")
        private final String key;

        @Schema(description = "넣은 요청값", example = "null")
        private final String value;

        @Schema(description = "오류 발생 이유", example = "공백일 수 없습니다")
        private final String reason;
    }
}
