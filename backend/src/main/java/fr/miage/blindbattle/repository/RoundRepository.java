package fr.miage.blindbattle.repository;

import fr.miage.blindbattle.domain.RoundEntity;
import fr.miage.blindbattle.domain.RoundStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoundRepository extends JpaRepository<RoundEntity, Long> {
    Optional<RoundEntity> findFirstByGameIdAndStatusOrderByNumberAsc(Long gameId, RoundStatus status);
    long countByGameIdAndStatusNot(Long gameId, RoundStatus status);
}
