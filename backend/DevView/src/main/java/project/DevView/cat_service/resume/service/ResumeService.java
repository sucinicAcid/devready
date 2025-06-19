package project.DevView.cat_service.resume.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.DevView.cat_service.resume.dto.ResumeRequest;
import project.DevView.cat_service.resume.dto.ResumeResponse;
import project.DevView.cat_service.resume.entity.Resume;
import project.DevView.cat_service.resume.entity.ResumeMessage;
import project.DevView.cat_service.resume.repository.ResumeRepository;
import project.DevView.cat_service.user.entity.UserEntity;
import project.DevView.cat_service.user.repository.UserRepository;
import project.DevView.cat_service.interview.service.InterviewFlowService;
import project.DevView.cat_service.resume.service.ResumeMessageService;
import project.DevView.cat_service.ai.service.ChatGptService;
import project.DevView.cat_service.ai.service.ResumeAIService;
import project.DevView.cat_service.resume.repository.TagQuestionRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final InterviewFlowService interviewFlowService;
    private final ResumeMessageService resumeMessageService;
    private final ChatGptService chatGptService;
    private final ResumeAIService resumeAIService;
    private final TagQuestionRepository tagQuestionRepository;

    @Transactional
    public ResumeResponse createResume(Long userId, ResumeRequest request) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Resume resume = Resume.builder()
            .user(user)
            .content(request.getContent())
            .build();

        return ResumeResponse.from(resumeRepository.save(resume));
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        return ResumeResponse.from(resume);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getUserResumes(Long userId) {
        return resumeRepository.findByUserId(userId).stream()
            .map(ResumeResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public ResumeResponse updateResume(Long resumeId, Long userId, ResumeRequest request) {
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        if (!resume.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Resume does not belong to the user");
        }

        resume.setContent(request.getContent());
        return ResumeResponse.from(resume);
    }

    @Transactional
    public void deleteResume(Long resumeId, Long userId) {
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        if (!resume.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Resume does not belong to the user");
        }

        resumeRepository.delete(resume);
    }

    @Transactional
    public String createFollowUpQuestion(Long resumeId, Long tagQuestionId, String answer) {
        log.info("[꼬리질문 생성 시작] resumeId: {}, tagQuestionId: {}, 마지막 답변: {}", resumeId, tagQuestionId, answer);
        
        // 1. 사용자의 답변을 저장
        resumeMessageService.saveAnswer(resumeId, tagQuestionId, answer);
        
        // 2. 태그 질문의 이전 대화 내용 조회
        List<ResumeMessage> messages = resumeMessageService.getMessagesByTagQuestion(resumeId, tagQuestionId);
        log.info("[이전 대화 내용 조회] 총 {}개의 메시지 조회됨", messages.size());
        
        // 3. 채팅 히스토리 형식으로 변환
        String chatHistory = messages.stream()
            .map(message -> String.format("%s : %s", 
                message.getType() == ResumeMessage.MessageType.QUESTION ? "질문" : "답변",
                message.getContent()))
            .collect(Collectors.joining("\n"));
        log.info("[채팅 히스토리]\n{}", chatHistory);

        // 4. AI 꼬리 질문 생성 프롬프트
        String prompt = String.format("""
                String costarPrompt = String.format(""\"
                [Context]
                당신은 개발자 기술 면접관입니다. 아래는 지금까지의 질문과 답변 내역입니다.

                %s

                마지막 답변: %s

                [Objective]
                지금까지의 문답 흐름을 고려해 **중복 없이** -- 특히 직전에 주고받은
                질문·답변을 깊이 파고드는 -- **심층 꼬리 질문**을 만들어라.

                [Style]
                - 실제 사람이 말하듯 자연스러운 **구어체** 한국어
                - 길이: 한두 문장
                - 기술 용어는 정확히, 불필요한 반복·군더더기 금지

                [Tone]
                친절하지만 면접관다운 전문성과 꼼꼼함을 유지

                [Audience]
                면접을 보는 **지원자(개발자)**

                [Response-format]
                질문만 출력
                ""\", chatHistory, answer);

                """, chatHistory, answer);
        log.info("[AI 프롬프트]\n{}", prompt);

        // 5. AI를 통해 꼬리 질문 생성
        String followUpQuestion = chatGptService.getCompletion(prompt);
        log.info("[생성된 꼬리질문] {}", followUpQuestion);
        
        // 6. 생성된 꼬리 질문을 저장
        resumeMessageService.saveQuestion(resumeId, tagQuestionId, followUpQuestion);
        
        // 7. 생성된 꼬리 질문 반환
        log.info("[꼬리질문 생성 완료] resumeId: {}, tagQuestionId: {}", resumeId, tagQuestionId);
        return followUpQuestion;
    }

    @Transactional(readOnly = true)
    public List<ResumeMessage> getMessages(Long resumeId) {
        return resumeMessageService.getAllMessages(resumeId);
    }

    @Transactional(readOnly = true)
    public List<ResumeMessage> getMessagesByTagQuestion(Long resumeId, Long tagQuestionId) {
        return resumeMessageService.getMessagesByTagQuestion(resumeId, tagQuestionId);
    }

    @Transactional(readOnly = true)
    public String evaluateMessages(Long resumeId) {
        // 1. 대화 기록 조회
        List<ResumeMessage> messages = resumeMessageService.getAllMessages(resumeId);
        
        // 2. 대화 기록을 문자열로 변환
        String chatHistory = messages.stream()
            .map(message -> String.format("%s : %s", 
                message.getType() == ResumeMessage.MessageType.QUESTION ? "면접관" : "지원자",
                message.getContent()))
            .collect(Collectors.joining("\n\n"));

        // 3. AI 평가 생성
        return chatGptService.evaluateAnswer(chatHistory);
    }

    @Transactional(readOnly = true)
    public boolean isAllQuestionsCompleted(Long resumeId) {
        log.info("이력서의 모든 질문 완료 상태 확인 - resumeId: {}", resumeId);
        
        // 이력서 존재 확인
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + resumeId));
        
        // 완료되지 않은 질문 개수 확인
        long incompleteQuestions = tagQuestionRepository.countIncompleteQuestionsByResumeId(resumeId);
        
        // 모든 질문이 완료되었는지 여부 반환
        return incompleteQuestions == 0;
    }
} 