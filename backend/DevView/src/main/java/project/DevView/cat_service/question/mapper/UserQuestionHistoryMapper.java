package project.DevView.cat_service.question.mapper;

import project.DevView.cat_service.question.dto.request.UserQuestionHistoryCreateRequestDto;
import project.DevView.cat_service.question.entity.UserQuestionHistory;
import project.DevView.cat_service.user.entity.UserEntity;
import project.DevView.cat_service.question.entity.Question;

import java.time.LocalDateTime;

public class UserQuestionHistoryMapper {

    /** Request DTO + 연관 엔티티 → UserQuestionHistory 엔티티 */
    public static UserQuestionHistory from(
            UserQuestionHistoryCreateRequestDto request,
            UserEntity user,
            Question question
    ) {
        return UserQuestionHistory.builder()
                .user(user)
                .question(question)
                .answeredAt(LocalDateTime.now())
                .completed(request.completed())
                .build();
    }
}
