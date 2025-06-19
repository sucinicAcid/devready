package project.DevView.cat_service.resume.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.DevView.cat_service.ai.service.ResumeAIService;
import project.DevView.cat_service.resume.dto.ResumeTagResponse;
import project.DevView.cat_service.resume.dto.TagQuestionResponse;
import project.DevView.cat_service.resume.entity.Resume;
import project.DevView.cat_service.resume.entity.ResumeTag;
import project.DevView.cat_service.resume.entity.TagQuestion;
import project.DevView.cat_service.resume.repository.ResumeRepository;
import project.DevView.cat_service.resume.repository.ResumeTagRepository;
import project.DevView.cat_service.resume.repository.TagQuestionRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeTagService {

    private final ResumeRepository resumeRepository;
    private final ResumeTagRepository resumeTagRepository;
    private final TagQuestionRepository tagQuestionRepository;
    private final ResumeAIService resumeAIService;

    @Transactional(readOnly = true)
    public boolean isGenerationCompleted(Long resumeId) {
        log.info("태그 및 질문 생성 완료 여부 확인 - resumeId: {}", resumeId);
        
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + resumeId));

        // 태그가 하나라도 있으면 생성이 완료된 것으로 간주
        return !resumeTagRepository.findByResumeId(resumeId).isEmpty();
    }

    @Transactional(readOnly = true)
    public ResumeTagResponse getTags(Long resumeId) {
        log.info("이력서 태그 조회 - resumeId: {}", resumeId);
        
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + resumeId));

        List<ResumeTag> tags = resumeTagRepository.findByResumeId(resumeId);
        
        return ResumeTagResponse.builder()
            .keywords(tags.stream()
                .map(tag -> ResumeTagResponse.KeywordItem.builder()
                    .tagType(tag.getTagType())
                    .keyword(tag.getKeyword())
                    .detail(tag.getDetail())
                    .depthScore(tag.getDepthScore())
                    .priorityScore(tag.getPriorityScore())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }

    @Transactional
    public ResumeTagResponse generateTagsAndQuestions(Long resumeId) {
        log.info("이력서 태그 및 질문 생성 시작 - resumeId: {}", resumeId);
        
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다: " + resumeId));

        // 이미 태그가 있는지 확인
        List<ResumeTag> existingTags = resumeTagRepository.findByResumeId(resumeId);
        if (!existingTags.isEmpty()) {
            log.info("이미 태그가 존재합니다. 태그 생성 건너뜁니다. - resumeId: {}, 태그 수: {}", resumeId, existingTags.size());
            return ResumeTagResponse.builder()
                .keywords(existingTags.stream()
                    .map(tag -> ResumeTagResponse.KeywordItem.builder()
                        .tagType(tag.getTagType())
                        .keyword(tag.getKeyword())
                        .detail(tag.getDetail())
                        .depthScore(tag.getDepthScore())
                        .priorityScore(tag.getPriorityScore())
                        .build())
                    .collect(Collectors.toList()))
                .build();
        }

        // 1. 태그 생성
        ResumeTagResponse tagResponse = resumeAIService.generateTags(resume.getContent());
        List<ResumeTag> tags = tagResponse.getKeywords().stream()
            .map(keyword -> ResumeTag.builder()
                .resume(resume)
                .keyword(keyword.getKeyword())
                .detail(keyword.getDetail())
                .tagType(keyword.getTagType())
                .depthScore(keyword.getDepthScore())
                .priorityScore(keyword.getPriorityScore())
                .build())
            .collect(Collectors.toList());

        // 2. 태그 저장
        tags = resumeTagRepository.saveAll(tags);
        log.info("{}개의 태그가 생성되었습니다.", tags.size());

        // 3. 각 태그별로 질문 생성
        for (ResumeTag tag : tags) {
            // 각 태그에 대해 질문 생성
            TagQuestionResponse questionResponse = resumeAIService.generateQuestions(List.of(tag));
            
            // 생성된 질문들을 저장
            List<TagQuestion> questions = questionResponse.getQuestions().stream()
                .map(q -> TagQuestion.builder()
                    .resumeTag(tag)
                    .baseQuestion(q.getBaseQuestion())
                    .createdQuestion(q.getCreatedQuestion())
                    .isCompleted(false)
                    .isAsked(false)
                    .build())
                .collect(Collectors.toList());

            tagQuestionRepository.saveAll(questions);
            log.info("태그 '{}'에 대해 {}개의 질문이 생성되었습니다.", tag.getKeyword(), questions.size());
        }

        return tagResponse;
    }

    @Transactional
    protected void saveTags(List<ResumeTag> tags) {
        resumeTagRepository.saveAll(tags);
    }
} 