package fr.miage.blindbattle.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "quiz_tracks", uniqueConstraints = @UniqueConstraint(columnNames = {"quiz_id", "track_id"}))
public class QuizTrackEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private QuizEntity quiz;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private TrackEntity track;
    @Column(nullable = false)
    private int position;

    protected QuizTrackEntity() {}

    public QuizTrackEntity(QuizEntity quiz, TrackEntity track, int position) {
        this.quiz = quiz;
        this.track = track;
        this.position = position;
    }

    public Long getId() { return id; }
    public QuizEntity getQuiz() { return quiz; }
    public TrackEntity getTrack() { return track; }
    public int getPosition() { return position; }

    public void moveTo(int position) { this.position = position; }
}
