package project.DevView.cat_service.question.entity;

import jakarta.persistence.*;
import lombok.*;
import project.DevView.cat_service.global.entity.TimeStamp;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "question")
@Getter @Setter
@NoArgsConstructor
public class Question extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Field field;

    @Column(length = 3600, nullable = false)
    private String question;

    @Column(length = 5000, nullable = false)
    private String answer;


    @Builder
    public Question(Long id,
                    Field field,
                    String question,
                    String answer) {
        this.id = id;
        this.field = field;
        this.question = question;
        this.answer = answer;
    }
}
