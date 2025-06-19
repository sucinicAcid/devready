package project.DevView.cat_service.question.repository;

import project.DevView.cat_service.question.entity.Field;
import project.DevView.cat_service.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByField(Enum field);

//    @Query("""
//        SELECT q
//        FROM Question q
//        JOIN q.fields f
//        WHERE f.name = :fieldName
//          AND q.id NOT IN :usedIds
//        ORDER BY q.id ASC
//    """)
//    List<Question> findAllNotUsed(
//            @Param("fieldName") String fieldName,
//            @Param("usedIds") List<Long> usedIds
//    );

    /**
     * 1) usedIds가 비었을 때 (빈 리스트)
     *    => f.name = :fieldName 만으로 검색
     *    => 오름차순, LIMIT 1
     */
    @Query(value = """
        SELECT q.*
        FROM question q
        WHERE UPPER(q.field) = UPPER(:fieldName)
        ORDER BY q.id ASC
        LIMIT 1
    """, nativeQuery = true)
    Optional<Question> findOneWithoutUsedIds(@Param("fieldName") String fieldName);

    @Query(value = """
        SELECT q.*
        FROM question q
        WHERE UPPER(q.field) = UPPER(:fieldName)
           AND q.id NOT IN (:usedIds)
         ORDER BY q.id ASC
         LIMIT 1
    """, nativeQuery = true)
    Optional<Question> findOneWithUsedIds(
            @Param("fieldName") String fieldName,
            @Param("usedIds") List<Long> usedIds
    );

    /**
     * 질문 내용으로 질문 찾기
     */
    @Query("SELECT q FROM Question q WHERE q.answer = :answer")
    Optional<Question> findByContent(@Param("answer") String answer);

}
