package project.DevView.cat_service.resume.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.DevView.cat_service.resume.entity.ResumeTag;

import java.util.List;

public interface ResumeTagRepository extends JpaRepository<ResumeTag, Long> {
    List<ResumeTag> findByResumeId(Long resumeId);

    @Query("SELECT rt FROM ResumeTag rt " +
           "WHERE rt.resume.id = :resumeId " +
           "AND rt.askedAll = false " +
           "ORDER BY rt.priorityScore DESC")
    List<ResumeTag> findUnaskedTagsByResumeIdOrderByPriorityDesc(Long resumeId);
} 