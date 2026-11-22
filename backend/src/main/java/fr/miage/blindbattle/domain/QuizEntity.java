package fr.miage.blindbattle.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "quizzes")
public class QuizEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity owner;
    @Column(nullable = false)
    private String title;
    @Column(length = 300)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizVisibility visibility;
    @Column(nullable = false, unique = true, updatable = false)
    private UUID shareToken;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    private Instant updatedAt;
    @Version
    private long version;
    @Formula("(select count(*) from quiz_tracks qt where qt.quiz_id = id)")
    private int trackCount;
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    private List<QuizTrackEntity> tracks = new ArrayList<>();

    protected QuizEntity() {}

    public QuizEntity(UserEntity owner, String title, String description, QuizVisibility visibility) {
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.visibility = visibility;
        this.shareToken = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public UserEntity getOwner() { return owner; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public QuizVisibility getVisibility() { return visibility; }
    public UUID getShareToken() { return shareToken; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<QuizTrackEntity> getTracks() { return tracks; }
    public int getTrackCount() { return trackCount; }

    public boolean isOwnedBy(long userId) { return owner.getId() == userId; }

    public boolean isPublic() { return visibility == QuizVisibility.PUBLIC; }

    public void update(String title, String description, QuizVisibility visibility) {
        this.title = title;
        this.description = description;
        this.visibility = visibility;
        this.updatedAt = Instant.now();
    }

    public void replaceTracks(List<TrackEntity> newTracks) {
        Map<Long, QuizTrackEntity> current = new HashMap<>();
        tracks.forEach(entry -> current.put(entry.getTrack().getId(), entry));
        Set<Long> kept = new HashSet<>();
        for (int index = 0; index < newTracks.size(); index++) {
            TrackEntity track = newTracks.get(index);
            QuizTrackEntity existing = current.get(track.getId());
            if (existing != null) {
                existing.moveTo(index + 1);
            } else {
                tracks.add(new QuizTrackEntity(this, track, index + 1));
            }
            kept.add(track.getId());
        }
        tracks.removeIf(entry -> !kept.contains(entry.getTrack().getId()));
        tracks.sort(Comparator.comparingInt(QuizTrackEntity::getPosition));
    }
}
