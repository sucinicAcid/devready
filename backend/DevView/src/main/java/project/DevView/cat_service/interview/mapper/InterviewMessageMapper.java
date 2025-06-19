package project.DevView.cat_service.interview.mapper;

import project.DevView.cat_service.interview.entity.Interview;
import project.DevView.cat_service.interview.entity.InterviewMessage;
import project.DevView.cat_service.question.entity.Question;

public class InterviewMessageMapper {

    public static InterviewMessage questionToMessage(Interview interview,
                                                     Question question) {
        return InterviewMessage.builder()
                .interview(interview)
                .sender("AI")
                .messageType("QUESTION")
                .content(question.getQuestion())
                .build();
    }

    public static InterviewMessage questionToMessageWithContent(Interview interview,
                                                               Question question,
                                                               String convertedContent) {
        return InterviewMessage.builder()
                .interview(interview)
                .sender("AI")
                .messageType("QUESTION")
                .content(convertedContent)
                .build();
    }

    public static InterviewMessage answerToMessage(Interview interview,
                                                   String userAnswer) {
        return InterviewMessage.builder()
                .interview(interview)
                .sender("USER")
                .messageType("ANSWER")
                .content(userAnswer)
                .build();
    }

    public static InterviewMessage followupToMessage(Interview interview,
                                                     String followUp) {
        return InterviewMessage.builder()
                .interview(interview)
                .sender("AI")
                .messageType("FOLLOWUP_QUESTION")
                .content(followUp)
                .build();
    }
}
