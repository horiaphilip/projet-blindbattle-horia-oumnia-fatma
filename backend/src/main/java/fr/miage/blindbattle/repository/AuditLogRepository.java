package fr.miage.blindbattle.repository;

import fr.miage.blindbattle.domain.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {}
