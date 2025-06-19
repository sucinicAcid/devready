package project.DevView.cat_service.question.repository;

import project.DevView.cat_service.question.entity.UserQuestionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserQuestionHistoryRepository extends JpaRepository<UserQuestionHistory, Long> {

    /**
     * userId가 이미 완료(completed=true)한 Question들의 ID 목록
     */
    @Query("""
        SELECT h.question.id
        FROM UserQuestionHistory h
        WHERE h.user.id = :userId
          AND h.completed = true
    """)
    List<Long> findAnsweredQuestionIds(@Param("userId") Long userId);
    
    /**
     * userId가 이미 완료한 UserQuestionHistory 목록
     */
    @Query("""
        SELECT h
        FROM UserQuestionHistory h
        WHERE h.user.id = :userId
          AND h.completed = true
    """)
    List<UserQuestionHistory> findAllCompletedByUserId(@Param("userId") Long userId);
    
    /**
     * 특정 필드에 대해 userId가 완료한 UserQuestionHistory 목록
     */
    @Query("""
        SELECT h
        FROM UserQuestionHistory h
        JOIN h.question q
        WHERE h.user.id = :userId
          AND UPPER(q.field) = UPPER(:fieldName)
          AND h.completed = true
    """)
    List<UserQuestionHistory> findAllCompletedByUserIdAndField(@Param("userId") Long userId, @Param("fieldName") String fieldName);

    /**
     * 특정 userId & questionId에 대해 새로운 UserQuestionHistory 생성
     * 
     * @Modifying + @Query => INSERT 쿼리를 실행할 수 있음.
     */
    @Modifying
    @Query(value = """
        INSERT INTO user_question_history (user_id, question_id, answered_at, completed, created_at, updated_at)
        SELECT :userId, :questionId, CURRENT_TIMESTAMP, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        WHERE NOT EXISTS (
            SELECT 1 FROM user_question_history h
            WHERE h.user_id = :userId
              AND h.question_id = :questionId
        )
    """, nativeQuery = true)
    void markQuestionCompleted(@Param("userId") Long userId,
                              @Param("questionId") Long questionId);
}
