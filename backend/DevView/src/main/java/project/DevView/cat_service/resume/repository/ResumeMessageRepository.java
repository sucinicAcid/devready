package project.DevView.cat_service.resume.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.DevView.cat_service.resume.entity.ResumeMessage;

import java.util.List;

public interface ResumeMessageRepository extends JpaRepository<ResumeMessage, Long> {
    
    /**
     * 이력서의 모든 메시지를 시간순으로 조회
     */
    List<ResumeMessage> findByResumeIdOrderByCreatedAtAsc(Long resumeId);

    /**
     * 이력서와 태그 질문에 해당하는 메시지를 시간순으로 조회
     */
    List<ResumeMessage> findByResumeIdAndTagQuestionIdOrderByCreatedAtAsc(Long resumeId, Long tagQuestionId);
} 