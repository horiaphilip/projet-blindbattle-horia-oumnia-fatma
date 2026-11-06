package fr.miage.blindbattle.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "players", uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "nickname"}))
public class PlayerEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GameEntity game;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private UserEntity user;
    @Column(nullable = false, length = 20)
    private String nickname;
    private int score;
    private int streak;
    @Column(nullable = false, updatable = false)
    private Instant joinedAt;
    private boolean guest;
    @Column(nullable = false, unique = true)
    private UUID token;
    @Version
    private long version;

    protected PlayerEntity() {}

    public PlayerEntity(GameEntity game, UserEntity user, String nickname) {
        this.game = game;
        this.user = user;
        this.nickname = nickname;
        this.guest = user == null;
        this.token = UUID.randomUUID();
        this.joinedAt = Instant.now();
    }

    public Long getId() { return id; }
    public GameEntity getGame() { return game; }
    public UserEntity getUser() { return user; }
    public String getNickname() { return nickname; }
    public int getScore() { return score; }
    public int getStreak() { return streak; }
    public Instant getJoinedAt() { return joinedAt; }
    public boolean isGuest() { return guest; }
    public UUID getToken() { return token; }
}
