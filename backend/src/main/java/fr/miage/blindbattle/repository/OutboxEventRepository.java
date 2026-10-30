package fr.miage.blindbattle.repository;

import fr.miage.blindbattle.domain.OutboxEventEntity;
import fr.miage.blindbattle.domain.OutboxStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {
    List<OutboxEventEntity> findTop50ByStatusOrderByIdAsc(OutboxStatus status);
}
