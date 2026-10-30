package fr.miage.blindbattle.repository;

import fr.miage.blindbattle.domain.UserStatsEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatsRepository extends JpaRepository<UserStatsEntity, Long> {
    Optional<UserStatsEntity> findByUserId(Long userId);
}
