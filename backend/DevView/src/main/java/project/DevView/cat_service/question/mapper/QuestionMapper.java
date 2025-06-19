package project.DevView.cat_service.question.mapper;

import project.DevView.cat_service.question.dto.request.QuestionCreateRequest;
import project.DevView.cat_service.question.entity.Field;
import project.DevView.cat_service.question.entity.Question;

import java.util.Set;

public class QuestionMapper {

    /** Request Record → Question 엔티티 */
    public static Question from(QuestionCreateRequest request) {
        return Question.builder()
                .question(request.question())
                .question(request.answer())
                .field(Field.fromName(request.field()))
                .build();
    }
}
