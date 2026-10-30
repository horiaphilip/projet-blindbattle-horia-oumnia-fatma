package fr.miage.blindbattle.repository;

import fr.miage.blindbattle.domain.ProcessedMessageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessageEntity, UUID> {}
