package project.DevView.cat_service.resume.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NextQuestionResponse {
    private final Long tagQuestionId;
    private final String question;
    private final String tagKeyword;
    private final String tagDetail;
} 