package project.DevView.cat_service.resume.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.DevView.cat_service.ai.service.ChatGptService;
import project.DevView.cat_service.resume.entity.Resume;
import project.DevView.cat_service.resume.entity.ResumeMessage;
import project.DevView.cat_service.resume.entity.TagQuestion;
import project.DevView.cat_service.resume.repository.ResumeMessageRepository;
import project.DevView.cat_service.resume.repository.ResumeRepository;
import project.DevView.cat_service.resume.repository.TagQuestionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ResumeMessageService {

    private final ResumeRepository resumeRepository;
    private final ResumeMessageRepository messageRepository;
    private final TagQuestionRepository tagQuestionRepository;
    private final ChatGptService chatGptService;

    /**
     * 질문 메시지 저장
     */
    public ResumeMessage saveQuestion(Long resumeId, Long tagQuestionId, String question) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + resumeId));
        
        TagQuestion tagQuestion = tagQuestionRepository.findById(tagQuestionId)
                .orElseThrow(() -> new IllegalArgumentException("태그 질문을 찾을 수 없습니다: " + tagQuestionId));
        
        ResumeMessage message = ResumeMessage.builder()
                .resume(resume)
                .type(ResumeMessage.MessageType.QUESTION)
                .tagQuestion(tagQuestion)
                .content(question)
                .build();
        
        return messageRepository.save(message);
    }

    /**
     * 답변 메시지 저장
     */
    public ResumeMessage saveAnswer(Long resumeId, Long tagQuestionId, String answer) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + resumeId));
        
        TagQuestion tagQuestion = tagQuestionRepository.findById(tagQuestionId)
                .orElseThrow(() -> new IllegalArgumentException("태그 질문을 찾을 수 없습니다: " + tagQuestionId));
        
        ResumeMessage message = ResumeMessage.builder()
                .resume(resume)
                .type(ResumeMessage.MessageType.ANSWER)
                .tagQuestion(tagQuestion)
                .content(answer)
                .build();
        
        return messageRepository.save(message);
    }

    /**
     * 이력서의 특정 태그 질문에 대한 모든 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<ResumeMessage> getMessagesByTagQuestion(Long resumeId, Long tagQuestionId) {
        return messageRepository.findByResumeIdAndTagQuestionIdOrderByCreatedAtAsc(resumeId, tagQuestionId);
    }

    /**
     * 이력서의 모든 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<ResumeMessage> getAllMessages(Long resumeId) {
        return messageRepository.findByResumeIdOrderByCreatedAtAsc(resumeId);
    }
} 