package fr.miage.blindbattle.domain;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "badges", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "code"}))
public class BadgeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;
    @Column(nullable = false)
    private String code;
    @Column(nullable = false)
    private Instant earnedAt;

    protected BadgeEntity() {}

    public BadgeEntity(UserEntity user, String code) {
        this.user = user;
        this.code = code;
        this.earnedAt = Instant.now();
    }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public String getCode() { return code; }
    public Instant getEarnedAt() { return earnedAt; }
}
