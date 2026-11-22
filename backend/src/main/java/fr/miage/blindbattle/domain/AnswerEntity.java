package fr.miage.blindbattle.domain;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "answers", uniqueConstraints = @UniqueConstraint(columnNames = {"round_id", "player_id"}))
public class AnswerEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RoundEntity round;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlayerEntity player;
    @Column(length = 200)
    private String rawAnswer;
    private Integer choiceIndex;
    @Column(nullable = false)
    private Instant receivedAt;
    private long responseMs;
    private double correctness;
    private int points;
    private String rejectedReason;

    protected AnswerEntity() {}

    private AnswerEntity(RoundEntity round, PlayerEntity player, String rawAnswer, Integer choiceIndex, Instant receivedAt) {
        this.round = round;
        this.player = player;
        this.rawAnswer = rawAnswer;
        this.choiceIndex = choiceIndex;
        this.receivedAt = receivedAt;
    }

    public static AnswerEntity accepted(RoundEntity round, PlayerEntity player, String rawAnswer, Integer choiceIndex,
            Instant receivedAt, long responseMs, double correctness, int points) {
        AnswerEntity answer = new AnswerEntity(round, player, rawAnswer, choiceIndex, receivedAt);
        answer.responseMs = responseMs;
        answer.correctness = correctness;
        answer.points = points;
        return answer;
    }

    public static AnswerEntity rejected(RoundEntity round, PlayerEntity player, String rawAnswer, Integer choiceIndex,
            Instant receivedAt, String reason) {
        AnswerEntity answer = new AnswerEntity(round, player, rawAnswer, choiceIndex, receivedAt);
        answer.rejectedReason = reason;
        return answer;
    }

    public Long getId() { return id; }
    public RoundEntity getRound() { return round; }
    public PlayerEntity getPlayer() { return player; }
    public String getRawAnswer() { return rawAnswer; }
    public Integer getChoiceIndex() { return choiceIndex; }
    public Instant getReceivedAt() { return receivedAt; }
    public long getResponseMs() { return responseMs; }
    public double getCorrectness() { return correctness; }
    public int getPoints() { return points; }
    public String getRejectedReason() { return rejectedReason; }
}
