package project.DevView.cat_service.resume.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.DevView.cat_service.resume.dto.NextQuestionResponse;
import project.DevView.cat_service.resume.entity.ResumeTag;
import project.DevView.cat_service.resume.entity.TagQuestion;
import project.DevView.cat_service.resume.repository.ResumeTagRepository;
import project.DevView.cat_service.resume.repository.TagQuestionRepository;
import project.DevView.cat_service.ai.service.ChatGptService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagQuestionService {

    private final ResumeTagRepository resumeTagRepository;
    private final TagQuestionRepository tagQuestionRepository;
    private final ResumeMessageService resumeMessageService;
    private final ChatGptService chatGptService;

    @Transactional
    public NextQuestionResponse getNextQuestion(Long resumeId) {
        // 1. 우선순위가 높은 미답변 태그 찾기
        List<ResumeTag> unaskedTags = resumeTagRepository.findUnaskedTagsByResumeIdOrderByPriorityDesc(resumeId);
        if (unaskedTags.isEmpty()) {
            throw new IllegalStateException("모든 태그에 대한 질문이 완료되었습니다.");
        }

        ResumeTag selectedTag = unaskedTags.get(0);  // 우선순위가 가장 높은 태그

        // 2. 해당 태그의 미답변 질문 찾기
        List<TagQuestion> unaskedQuestions = tagQuestionRepository.findUnaskedQuestionsByResumeTagId(selectedTag.getId());
        if (unaskedQuestions.isEmpty()) {
            // 현재 태그의 모든 질문이 완료되었으므로 태그를 완료 처리
            selectedTag.setAskedAll(true);
            // 다음 태그의 질문을 가져오기 위해 재귀 호출
            return getNextQuestion(resumeId);
        }

        // 3. 랜덤하게 하나 선택
        TagQuestion selectedQuestion = unaskedQuestions.get(0);  // 이미 RANDOM()으로 정렬되어 있음
        
        // 4. 질문을 asked로 표시하고 꼬리 질문 카운트 증가
        selectedQuestion.setAsked(true);
        selectedQuestion.setFollowUpCount(selectedQuestion.getFollowUpCount() + 1);
        
        // 5. 꼬리 질문이 2번 완료되었으면 질문을 완료 처리
        if (selectedQuestion.getFollowUpCount() >= 2) {
            selectedQuestion.setCompleted(true);
            
            // 태그의 모든 질문이 완료되었는지 확인
            long remainingIncomplete = tagQuestionRepository.countByResumeTagIdAndIsCompletedFalse(selectedTag.getId());
            if (remainingIncomplete == 0) {
                selectedTag.setAskedAll(true);
            }
        }

        // 6. 질문을 ResumeMessage에도 저장
        resumeMessageService.saveQuestion(resumeId, selectedQuestion.getId(), selectedQuestion.getCreatedQuestion());

        // 7. NextQuestionResponse 생성하여 반환
        return NextQuestionResponse.builder()
            .tagQuestionId(selectedQuestion.getId())
            .question(selectedQuestion.getCreatedQuestion())
            .tagKeyword(selectedTag.getKeyword())
            .tagDetail(selectedTag.getDetail())
            .build();
    }


    @Transactional
    public NextQuestionResponse getNextQuestionForSkip(Long resumeId) {
        // 1. 우선순위가 높은 미답변 태그 찾기
        List<ResumeTag> unaskedTags = resumeTagRepository.findUnaskedTagsByResumeIdOrderByPriorityDesc(resumeId);
        if (unaskedTags.isEmpty()) {
            throw new IllegalStateException("모든 태그에 대한 질문이 완료되었습니다.");
        }

        ResumeTag selectedTag = unaskedTags.get(0);  // 우선순위가 가장 높은 태그

        // 2. 해당 태그의 미답변 질문 찾기
        List<TagQuestion> unaskedQuestions = tagQuestionRepository.findUnaskedQuestionsByResumeTagId(selectedTag.getId());
        if (unaskedQuestions.isEmpty()) {
            // 현재 태그의 모든 질문이 완료되었으므로 태그를 완료 처리
            selectedTag.setAskedAll(true);
            // 다음 태그의 질문을 가져오기 위해 재귀 호출
            return getNextQuestion(resumeId);
        }

        // 3. 랜덤하게 하나 선택
        TagQuestion selectedQuestion = unaskedQuestions.get(0);  // 이미 RANDOM()으로 정렬되어 있음
        
        // 4. 질문을 asked로 표시하고 꼬리 질문 카운트 증가
        selectedQuestion.setAsked(true);
        selectedQuestion.setFollowUpCount(selectedQuestion.getFollowUpCount() + 1);
        
        // 5. 꼬리 질문이 2번 완료되었으면 질문을 완료 처리
        if (selectedQuestion.getFollowUpCount() >= 2) {
            selectedQuestion.setCompleted(true);
            
            // 태그의 모든 질문이 완료되었는지 확인
            long remainingIncomplete = tagQuestionRepository.countByResumeTagIdAndIsCompletedFalse(selectedTag.getId());
            if (remainingIncomplete == 0) {
                selectedTag.setAskedAll(true);
            }
        }

        // 6. 질문을 AI를 통해 가다듬기
        String refinedQuestion = chatGptService.refineQuestionAfterSkip(selectedQuestion.getCreatedQuestion());

        // 7. 가다듬어진 질문을 ResumeMessage에 저장
        resumeMessageService.saveQuestion(resumeId, selectedQuestion.getId(), refinedQuestion);

        // 8. NextQuestionResponse 생성하여 반환
        return NextQuestionResponse.builder()
            .tagQuestionId(selectedQuestion.getId())
            .question(refinedQuestion)  // 가다듬어진 질문 사용
            .tagKeyword(selectedTag.getKeyword())
            .tagDetail(selectedTag.getDetail())
            .build();
    }


    @Transactional
    public void markQuestionAsCompleted(Long resumeId, Long questionId) {
        TagQuestion question = tagQuestionRepository.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (!question.getResumeTag().getResume().getId().equals(resumeId)) {
            throw new IllegalArgumentException("Question does not belong to the specified resume");
        }

        // 질문 완료 처리
        question.setCompleted(true);

        // 태그의 모든 질문이 완료되었는지 확인
        ResumeTag tag = question.getResumeTag();
        long remainingIncomplete = tagQuestionRepository.countByResumeTagIdAndIsCompletedFalse(tag.getId());
        if (remainingIncomplete == 0) {
            tag.setAskedAll(true);
        }
    }
} 