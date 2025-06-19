package project.DevView.cat_service.resume.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.DevView.cat_service.resume.entity.Resume;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserId(Long userId);
} 