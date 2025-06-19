package project.DevView.cat_service.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.DevView.cat_service.ai.service.ChatGptService;
import project.DevView.cat_service.interview.entity.InterviewMessage;
import project.DevView.cat_service.interview.repository.InterviewMessageRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowUpQuestionService {
    
    private final ChatGptService chatGptService;
    private final InterviewMessageRepository messageRepository;

    public String generateFollowUpQuestion(Long interviewId, String userAnswer) {
        // 현재 인터뷰의 대화 기록을 가져옴
        List<InterviewMessage> messages = messageRepository.findByInterview_IdOrderByCreatedAtAsc(interviewId);
        
        // 대화 컨텍스트 생성
        String conversationContext = messages.stream()
            .map(msg -> String.format("%s: %s", msg.getSender(), msg.getContent()))
            .collect(Collectors.joining("\n"));

        // GPT를 통해 꼬리질문 생성
        return chatGptService.generateFollowUpQuestion(conversationContext, userAnswer);
    }
} 