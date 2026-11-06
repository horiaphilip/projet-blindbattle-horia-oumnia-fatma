package fr.miage.blindbattle.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "rounds", uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "number"}))
public class RoundEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GameEntity game;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private TrackEntity track;
    @Column(nullable = false)
    private int number;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoundStatus status;
    private Instant startedAt;
    private Instant endsAt;
    @ElementCollection
    @CollectionTable(name = "round_choices", joinColumns = @JoinColumn(name = "round_id"))
    @OrderColumn(name = "position")
    @Column(name = "track_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Long> choiceTrackIds = new ArrayList<>();

    protected RoundEntity() {}

    public RoundEntity(GameEntity game, TrackEntity track, int number) {
        this.game = game;
        this.track = track;
        this.number = number;
        this.status = RoundStatus.PENDING;
    }

    public Long getId() { return id; }
    public GameEntity getGame() { return game; }
    public TrackEntity getTrack() { return track; }
    public int getNumber() { return number; }
    public RoundStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndsAt() { return endsAt; }
    public List<Long> getChoiceTrackIds() { return choiceTrackIds; }

    public void start(Instant now, int seconds) {
        this.status = RoundStatus.PLAYING;
        this.startedAt = now;
        this.endsAt = now.plusSeconds(seconds);
    }

    public void end() { this.status = RoundStatus.ENDED; }
}
