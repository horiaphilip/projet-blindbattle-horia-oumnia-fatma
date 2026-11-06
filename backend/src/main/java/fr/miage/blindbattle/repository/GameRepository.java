package fr.miage.blindbattle.repository;

import fr.miage.blindbattle.domain.GameEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<GameEntity, Long> {
    Optional<GameEntity> findByCode(String code);
    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"host", "players"})
    Optional<GameEntity> findWithPlayersByCode(String code);
}
