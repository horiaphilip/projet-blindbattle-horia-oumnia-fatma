package fr.miage.blindbattle.repository;

import fr.miage.blindbattle.domain.PlayerEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    boolean existsByGameIdAndNicknameIgnoreCase(Long gameId, String nickname);
    long countByGameId(Long gameId);
    Optional<PlayerEntity> findByToken(UUID token);
}
