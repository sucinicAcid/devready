package project.DevView.cat_service.question.mapper;

import project.DevView.cat_service.question.dto.request.BookmarkCreateRequestDto;
import project.DevView.cat_service.question.entity.Bookmark;
import project.DevView.cat_service.user.entity.UserEntity;
import project.DevView.cat_service.interview.entity.InterviewMessage;

public class BookmarkMapper {

    /** Request DTO + 연관 엔티티 → Bookmark 엔티티 */
    public static Bookmark from(
            BookmarkCreateRequestDto request,
            UserEntity user,
            InterviewMessage message
    ) {
        return Bookmark.builder()
                .user(user)
                .message(message)
                .build();
    }
}
