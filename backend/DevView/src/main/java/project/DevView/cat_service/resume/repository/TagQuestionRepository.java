package project.DevView.cat_service.resume.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.DevView.cat_service.resume.entity.TagQuestion;

import java.util.List;

public interface TagQuestionRepository extends JpaRepository<TagQuestion, Long> {

    /** 아직 안 물어본 질문 중 무작위 1/N(정렬) 전체 조회  */
    @Query("""
           SELECT tq
           FROM TagQuestion tq
           WHERE tq.resumeTag.id = :resumeTagId
             AND tq.isAsked = false
           ORDER BY function('RAND')
           """)
    List<TagQuestion> findUnaskedQuestionsByResumeTagId(@Param("resumeTagId") Long resumeTagId);

    /** 아직 안 물어본 질문 개수 */
    @Query("""
           SELECT COUNT(tq)
           FROM TagQuestion tq
           WHERE tq.resumeTag.id = :resumeTagId
             AND tq.isAsked = false
           """)
    long countUnaskedQuestionsByResumeTagId(@Param("resumeTagId") Long resumeTagId);

    /** 아직 답변 완료되지 않은 질문 개수 */
    @Query("""
           SELECT COUNT(tq)
           FROM TagQuestion tq
           WHERE tq.resumeTag.id = :resumeTagId
             AND tq.isCompleted = false
           """)
    long countByResumeTagIdAndIsCompletedFalse(@Param("resumeTagId") Long resumeTagId);

    /** 특정 이력서의 모든 질문 중 완료되지 않은 질문 개수 */
    @Query("""
           SELECT COUNT(tq)
           FROM TagQuestion tq
           JOIN tq.resumeTag rt
           WHERE rt.resume.id = :resumeId
             AND tq.isCompleted = false
           """)
    long countIncompleteQuestionsByResumeId(@Param("resumeId") Long resumeId);

    /** 특정 이력서의 모든 질문 개수 */
    @Query("""
           SELECT COUNT(tq)
           FROM TagQuestion tq
           JOIN tq.resumeTag rt
           WHERE rt.resume.id = :resumeId
           """)
    long countTotalQuestionsByResumeId(@Param("resumeId") Long resumeId);
}
