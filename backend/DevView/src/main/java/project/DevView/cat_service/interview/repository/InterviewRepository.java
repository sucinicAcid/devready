package project.DevView.cat_service.interview.repository;

import project.DevView.cat_service.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import project.DevView.cat_service.question.entity.Field;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    // 사용자 + 분야로 인터뷰 목록 찾기
    List<Interview> findByUserIdAndField(Long userId, Field field);

    // 필요하다면 다른 쿼리 메서드 추가
}
