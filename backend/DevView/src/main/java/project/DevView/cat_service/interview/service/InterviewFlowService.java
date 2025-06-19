// src/main/java/project/DevView/cat_service/interview/service/InterviewFlowService.java
package project.DevView.cat_service.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.DevView.cat_service.ai.service.ChatGptService;
import project.DevView.cat_service.global.exception.CustomException;
import project.DevView.cat_service.global.exception.ErrorCode;
import project.DevView.cat_service.interview.entity.Interview;
import project.DevView.cat_service.interview.entity.InterviewMessage;
import project.DevView.cat_service.interview.mapper.InterviewMessageMapper;
import project.DevView.cat_service.interview.repository.InterviewMessageRepository;
import project.DevView.cat_service.interview.repository.InterviewRepository;
import project.DevView.cat_service.question.entity.Question;
import project.DevView.cat_service.question.entity.UserQuestionHistory;
import project.DevView.cat_service.question.repository.QuestionRepository;
import project.DevView.cat_service.question.repository.UserQuestionHistoryRepository;
import project.DevView.cat_service.user.repository.UserRepository;
import project.DevView.cat_service.resume.entity.Resume;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InterviewFlowService {

    private final InterviewRepository            interviewRepository;
    private final QuestionRepository             questionRepository;
    private final InterviewMessageRepository     messageRepository;
    private final UserQuestionHistoryRepository  historyRepository;
    private final UserRepository                 userRepository;
    private final ChatGptService                 chatGptService;

    /**
     * (1) 아직 답변 안 한 질문 한 개 꺼내기
     */
    public Question getNextQuestion(long userId, Long interviewId) {
        Interview iv = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_EXIST));

        List<Long> used = historyRepository.findAnsweredQuestionIds(userId);
        Optional<Question> q = used.isEmpty()
                ? questionRepository.findOneWithoutUsedIds(iv.getField().toString())
                : questionRepository.findOneWithUsedIds(iv.getField().toString(), used);

        // 질문을 찾았다면 UserQuestionHistory 생성 및 완료 처리
        q.ifPresent(question -> {
            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));
            
            // UserQuestionHistory 생성
            var history = UserQuestionHistory.builder()
                    .user(user)
                    .question(question)
                    .answeredAt(LocalDateTime.now())
                    .completed(true)
                    .build();
            
            historyRepository.save(history);
        });

        return q.orElse(null);
    }

    /**
     * 질문을 COSTAR 형식으로 변환하여 반환
     */
    public String getConvertedQuestionContent(Question question) {
        return chatGptService.convertQuestionToCostarFormat(question.getQuestion());
    }

    /**
     * (2) AI 질문 메시지 저장
     */
    public InterviewMessage createQuestionMessage(Long interviewId, Question q) {
        Interview iv = loadInterview(interviewId);
        InterviewMessage msg = InterviewMessageMapper.questionToMessage(iv, q);
        // ↓ setCreatedAt() 지우고 Auditing에 맡긴다
        return messageRepository.save(msg);
    }

    /**
     * (2) AI 질문 메시지 저장 (변환된 내용 사용)
     */
    public InterviewMessage createQuestionMessage(Long interviewId, Question q, String convertedContent) {
        Interview iv = loadInterview(interviewId);
        InterviewMessage msg = InterviewMessageMapper.questionToMessageWithContent(iv, q, convertedContent);
        return messageRepository.save(msg);
    }

    /**
     * (3) 사용자 답변 메시지 저장
     */
    public InterviewMessage createAnswerMessage(Long interviewId, String answer) {
        Interview iv = loadInterview(interviewId);
        InterviewMessage msg = InterviewMessageMapper.answerToMessage(iv, answer);
        return messageRepository.save(msg);
    }

    /**
     * (4) ChatGPT로 꼬리질문 생성 & 저장
     */
    public InterviewMessage createFollowUpQuestion(Long interviewId, String answer) {
        Interview iv = loadInterview(interviewId);
        String context = buildContext(interviewId);
        String followUp = chatGptService.generateFollowUpQuestion(context, answer);
        InterviewMessage msg = InterviewMessageMapper.followupToMessage(iv, followUp);
        return messageRepository.save(msg);
    }

    /**
     * (5) 전체 메시지 리스트
     */
    public List<InterviewMessage> getMessages(Long interviewId) {
        return messageRepository.findByInterview_IdOrderByCreatedAtAsc(interviewId);
    }

    /**
     * (6) 현재 꼬리질문 종료 처리
     */
    public void finishCurrentQuestion(Long interviewId, long userId) {
        Interview iv = loadInterview(interviewId);
        // primitive int 비교
        if (iv.getUser().getId() != userId) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        // question 필드가 제거되었으므로 이 메서드는 더 이상 필요하지 않습니다.
        // 대신 인터뷰 종료 시점에 모든 질문을 완료 처리하는 방식으로 변경할 수 있습니다.
    }

    @Transactional(readOnly = true)
    public String evaluateMessages(Long interviewId) {
        // 1. 메시지 조회
        List<InterviewMessage> messages = messageRepository.findByInterview_IdOrderByCreatedAtAsc(interviewId);
        
        // 2. 채팅 히스토리 형식으로 변환
        String chatHistory = messages.stream()
            .map(message -> String.format("%s : %s", 
                message.getMessageType().equals("QUESTION") ? "질문" : "답변",
                message.getContent()))
            .collect(Collectors.joining("\n"));

        // 3. AI 평가 요청 - CS 전용 evaluateCSAnswer 메서드 사용
        return chatGptService.evaluateCSAnswer(chatHistory);
    }

    /* internal */
    private Interview loadInterview(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_EXIST));
    }

    private String buildContext(Long interviewId) {
        var msgs = messageRepository.findByInterview_IdOrderByCreatedAtAsc(interviewId);
        var sb = new StringBuilder();
        msgs.forEach(m -> sb.append("[").append(m.getSender()).append("] ")
                .append(m.getContent()).append("\n"));
        return sb.toString();
    }
}
