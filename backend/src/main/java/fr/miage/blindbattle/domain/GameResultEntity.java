package fr.miage.blindbattle.domain;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "game_results", uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "player_id"}))
public class GameResultEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GameEntity game;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlayerEntity player;
    @Column(name = "player_rank", nullable = false)
    private int rank;
    private int finalScore;
    private int correctAnswers;
    private long avgResponseMs;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected GameResultEntity() {}

    public GameResultEntity(GameEntity game, PlayerEntity player, int rank, int finalScore, int correctAnswers,
            long avgResponseMs) {
        this.game = game;
        this.player = player;
        this.rank = rank;
        this.finalScore = finalScore;
        this.correctAnswers = correctAnswers;
        this.avgResponseMs = avgResponseMs;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public GameEntity getGame() { return game; }
    public PlayerEntity getPlayer() { return player; }
    public int getRank() { return rank; }
    public int getFinalScore() { return finalScore; }
    public int getCorrectAnswers() { return correctAnswers; }
    public long getAvgResponseMs() { return avgResponseMs; }
    public Instant getCreatedAt() { return createdAt; }
}
