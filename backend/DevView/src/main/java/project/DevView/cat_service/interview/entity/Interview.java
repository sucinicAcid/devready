package project.DevView.cat_service.interview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import project.DevView.cat_service.global.entity.TimeStamp;
import project.DevView.cat_service.question.entity.Field;
import project.DevView.cat_service.user.entity.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview")
@Getter @Setter
@NoArgsConstructor
public class Interview extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private Field field;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    @Builder
    public Interview(Long id,
                     UserEntity user,
                     Field field,
                     LocalDateTime startedAt,
                     LocalDateTime endedAt) {
        this.id = id;
        this.user = user;
        this.field = field;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }
}
