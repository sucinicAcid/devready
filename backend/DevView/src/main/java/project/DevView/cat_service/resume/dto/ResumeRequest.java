package project.DevView.cat_service.resume.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ResumeRequest {
    @NotBlank(message = "이력서 내용은 필수입니다")
    @Size(min = 100, max = 10000, message = "이력서 내용은 100자 이상 10000자 이하여야 합니다")
    private final String content;

    public ResumeRequest(String content) {
        this.content = content;
    }
} 