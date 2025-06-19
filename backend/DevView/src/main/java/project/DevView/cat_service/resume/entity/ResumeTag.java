package project.DevView.cat_service.resume.entity;

import jakarta.persistence.*;
import lombok.*;
import project.DevView.cat_service.global.entity.TimeStamp;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "resume_tag")
@Getter @Setter
@NoArgsConstructor
public class ResumeTag extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TagType tagType;

    @Column(nullable = false, length = 200)
    private String keyword;  // 태그에서 추출된 키워드

    @Column(nullable = false, length = 1000)
    private String detail;   // 태그 관련 상세 내용

    @Column(nullable = false)
    private int depthScore;  // 태그의 깊이 점수

    @Column(nullable = false)
    private int priorityScore;  // 태그의 우선순위 점수

    @Column(nullable = false)
    private boolean askedAll;  // 모든 질문이 한 번이라도 제시되었는지 여부

    @OneToMany(mappedBy = "resumeTag", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TagQuestion> questions = new HashSet<>();

    @Builder
    public ResumeTag(Long id,
                    Resume resume,
                    TagType tagType,
                    String keyword,
                    String detail,
                    int depthScore,
                    int priorityScore,
                    boolean askedAll) {
        this.id = id;
        this.resume = resume;
        this.tagType = tagType;
        this.keyword = keyword;
        this.detail = detail;
        this.depthScore = depthScore;
        this.priorityScore = priorityScore;
        this.askedAll = askedAll;
    }

    public void addQuestion(TagQuestion question) {
        this.questions.add(question);
        question.setResumeTag(this);
    }

    // 태그 타입에 따른 기본 질문들을 가져오는 편의 메서드
    public List<String> getBaseQuestions() {
        return this.tagType.getBaseQuestions();
    }
} 