package fr.miage.blindbattle.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "games")
public class GameEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 6)
    private String code;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity host;
    @Embedded
    private GameSettings settings;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Version
    private long version;
    @OneToMany(mappedBy = "game")
    @OrderBy("joinedAt")
    private List<PlayerEntity> players = new ArrayList<>();
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    @OrderBy("number")
    private List<RoundEntity> rounds = new ArrayList<>();

    protected GameEntity() {}

    public GameEntity(String code, UserEntity host, GameSettings settings) {
        this.code = code;
        this.host = host;
        this.settings = settings;
        this.status = GameStatus.LOBBY;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public GameStatus getStatus() { return status; }
    public UserEntity getHost() { return host; }
    public GameSettings getSettings() { return settings; }
    public Instant getCreatedAt() { return createdAt; }
    public List<PlayerEntity> getPlayers() { return players; }
    public List<RoundEntity> getRounds() { return rounds; }

    public boolean isHost(long userId) { return host.getId() == userId; }

    public void addRound(TrackEntity track) { rounds.add(new RoundEntity(this, track, rounds.size() + 1)); }

    public void start() { this.status = GameStatus.PLAYING; }

    public void finish() { this.status = GameStatus.FINISHED; }
}
