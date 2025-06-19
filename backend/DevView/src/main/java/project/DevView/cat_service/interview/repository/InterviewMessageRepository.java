package project.DevView.cat_service.interview.repository;

import project.DevView.cat_service.interview.entity.InterviewMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * InterviewMessageRepository
 * - InterviewMessage 엔티티를 관리하는 Spring Data JPA 인터페이스
 */
public interface InterviewMessageRepository extends JpaRepository<InterviewMessage, Long> {

    /**
     * 1) 특정 인터뷰(Interview.id = :interviewId)의 모든 메시지를
     *    createdAt 오름차순으로 조회 (대화 순서대로 표시 용도)
     */
    List<InterviewMessage> findByInterview_IdOrderByCreatedAtAsc(Long interviewId);
}
