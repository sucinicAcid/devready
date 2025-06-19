package project.DevView.cat_service.question.entity;

import jakarta.persistence.*;
import lombok.*;
import project.DevView.cat_service.global.entity.TimeStamp;
import project.DevView.cat_service.user.entity.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_question_history")
@Getter @Setter
@NoArgsConstructor
public class UserQuestionHistory extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 사용자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // 어떤 질문
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    // 사용자가 이 질문을 최종적으로 답변 완료한 시각
    private LocalDateTime answeredAt;

    // 다 끝냈는지, 중간 스킵했는지 등 상태 구분
    private Boolean completed;

    @Builder
    public UserQuestionHistory(Long id,
                               UserEntity user,
                               Question question,
                               LocalDateTime answeredAt,
                               Boolean completed) {
        this.id = id;
        this.user = user;
        this.question = question;
        this.answeredAt = answeredAt;
        this.completed = completed;
    }
}
