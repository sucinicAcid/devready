package project.DevView.cat_service.question.entity;

import jakarta.persistence.*;
import lombok.*;
import project.DevView.cat_service.global.entity.TimeStamp;
import project.DevView.cat_service.interview.entity.InterviewMessage;
import project.DevView.cat_service.user.entity.UserEntity;

@Entity
@Table(name = "bookmark")
@Getter @Setter
@NoArgsConstructor
public class Bookmark extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 북마크했는지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // 어떤 메시지를 북마크했는지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id")
    private InterviewMessage message;

    @Builder
    public Bookmark(Long id,
                    UserEntity user,
                    InterviewMessage message) {
        this.id = id;
        this.user = user;
        this.message = message;
    }
}
