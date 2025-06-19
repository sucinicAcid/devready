package project.DevView.cat_service.resume.entity;

import jakarta.persistence.*;
import lombok.*;
import project.DevView.cat_service.global.entity.TimeStamp;

@Entity
@Table(name = "tag_question")
@Getter @Setter
@NoArgsConstructor
public class TagQuestion extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_tag_id", nullable = false)
    private ResumeTag resumeTag;

    @Column(nullable = false)
    private String baseQuestion;    // 기본 질문 (프롬프트에서 정의된)

    @Column(nullable = false, length = 1000)
    private String createdQuestion; // AI가 생성한 실제 질문

    @Column(nullable = false)
    private boolean isCompleted;    // 질문 완료 여부

    @Column(nullable = false)
    private boolean isAsked;  // 질문이 한 번이라도 제시되었는지 여부

    @Column(nullable = false)
    private int followUpCount;

    public void setResumeTag(ResumeTag resumeTag) {
        this.resumeTag = resumeTag;
    }

    @Builder
    public TagQuestion(Long id,
                      ResumeTag resumeTag,
                      String baseQuestion,
                      String createdQuestion,
                      boolean isCompleted,
                      boolean isAsked) {
        this.id = id;
        this.resumeTag = resumeTag;
        this.baseQuestion = baseQuestion;
        this.createdQuestion = createdQuestion;
        this.isCompleted = isCompleted;
        this.isAsked = isAsked;
        this.followUpCount = 0;
    }

    public void setAsked(boolean asked) {
        isAsked = asked;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setFollowUpCount(int followUpCount) {
        this.followUpCount = followUpCount;
    }
} 