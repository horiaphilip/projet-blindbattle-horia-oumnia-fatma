package fr.miage.blindbattle.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String type;
    private Long userId;
    @Column(length = 1000)
    private String details;
    @Column(nullable = false)
    private Instant createdAt;

    protected AuditLogEntity() {}

    public AuditLogEntity(String type, Long userId, String details) {
        this.type = type;
        this.userId = userId;
        this.details = details;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public Long getUserId() { return userId; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}
