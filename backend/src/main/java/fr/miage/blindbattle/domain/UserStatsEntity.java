package fr.miage.blindbattle.domain;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "user_stats")
public class UserStatsEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;
    private int gamesPlayed;
    private long totalScore;
    private int bestScore;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected UserStatsEntity() {}

    public UserStatsEntity(UserEntity user) {
        this.user = user;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public int getGamesPlayed() { return gamesPlayed; }
    public long getTotalScore() { return totalScore; }
    public int getBestScore() { return bestScore; }
    public Instant getCreatedAt() { return createdAt; }
}
