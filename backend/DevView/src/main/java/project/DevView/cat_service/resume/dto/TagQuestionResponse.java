package project.DevView.cat_service.resume.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TagQuestionResponse {
    private List<QuestionItem> questions;

    @Getter
    @Builder
    public static class QuestionItem {
        private Long id;
        private Long tagId;
        private String keyword;
        private String detail;
        private String baseQuestion;
        private String createdQuestion;
    }
} 