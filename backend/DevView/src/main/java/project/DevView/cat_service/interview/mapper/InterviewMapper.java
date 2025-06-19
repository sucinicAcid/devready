package project.DevView.cat_service.interview.mapper;

import project.DevView.cat_service.interview.dto.request.InterviewCreateRequestDto;
import project.DevView.cat_service.interview.entity.Interview;
import project.DevView.cat_service.question.entity.Field;
import project.DevView.cat_service.user.entity.UserEntity;

import java.time.LocalDateTime;

public class InterviewMapper {

    /** Request + User + Field → Interview 엔티티 */
    public static Interview from(InterviewCreateRequestDto req,
                                 UserEntity user) {
        return Interview.builder()
                .user(user)
                .field(Field.fromName(req.field()))
                .startedAt(LocalDateTime.now())
                .build();
    }

    /** Interview 엔티티 → Response DTO */
    public static project.DevView.cat_service.interview.dto.response.InterviewResponseDto toDto(Interview iv) {
        return project.DevView.cat_service.interview.dto.response.InterviewResponseDto.of(iv);
    }
}
