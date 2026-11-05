package fr.miage.blindbattle.repository;

import fr.miage.blindbattle.domain.TrackEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<TrackEntity, Long> {
    Optional<TrackEntity> findByDeezerId(long deezerId);
}
