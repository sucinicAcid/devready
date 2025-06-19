package project.DevView.cat_service.resume.entity;

import jakarta.persistence.*;
import lombok.*;
import project.DevView.cat_service.global.entity.TimeStamp;
import project.DevView.cat_service.user.entity.UserEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resume")
@Getter @Setter
@NoArgsConstructor
public class Resume extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false, length = 10000)
    private String content;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ResumeTag> tags = new HashSet<>();

    @Builder
    public Resume(Long id,
                 UserEntity user,
                 String content) {
        this.id = id;
        this.user = user;
        this.content = content;
    }

    public void addTag(ResumeTag tag) {
        this.tags.add(tag);
        tag.setResume(this);
    }
} 