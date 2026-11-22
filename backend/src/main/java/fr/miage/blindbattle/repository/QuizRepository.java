package fr.miage.blindbattle.repository;

import fr.miage.blindbattle.domain.QuizEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<QuizEntity, Long> {
    Page<QuizEntity> findByOwnerId(long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"tracks", "tracks.track", "owner"})
    Optional<QuizEntity> findWithTracksById(long id);

    @EntityGraph(attributePaths = {"tracks", "tracks.track", "owner"})
    Optional<QuizEntity> findWithTracksByShareToken(UUID shareToken);
}
