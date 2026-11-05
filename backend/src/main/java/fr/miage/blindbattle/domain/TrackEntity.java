package fr.miage.blindbattle.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "tracks")
public class TrackEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private long deezerId;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String artist;
    private long artistId;
    private String album;
    private String coverUrl;
    private int rank;
    private int durationSeconds;
    private boolean hasPreview;

    protected TrackEntity() {}

    public TrackEntity(long deezerId, String title, String artist, long artistId, String album, String coverUrl,
            int rank, int durationSeconds, boolean hasPreview) {
        this.deezerId = deezerId;
        refresh(title, artist, artistId, album, coverUrl, rank, durationSeconds, hasPreview);
    }

    public Long getId() { return id; }
    public long getDeezerId() { return deezerId; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public long getArtistId() { return artistId; }
    public String getAlbum() { return album; }
    public String getCoverUrl() { return coverUrl; }
    public int getRank() { return rank; }
    public int getDurationSeconds() { return durationSeconds; }
    public boolean hasPreview() { return hasPreview; }

    public void refresh(String title, String artist, long artistId, String album, String coverUrl, int rank,
            int durationSeconds, boolean hasPreview) {
        this.title = title;
        this.artist = artist;
        this.artistId = artistId;
        this.album = album;
        this.coverUrl = coverUrl;
        this.rank = rank;
        this.durationSeconds = durationSeconds;
        this.hasPreview = hasPreview;
    }
}
