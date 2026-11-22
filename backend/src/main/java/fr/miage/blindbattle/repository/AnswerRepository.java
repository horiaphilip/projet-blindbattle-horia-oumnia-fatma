package fr.miage.blindbattle.repository;

import fr.miage.blindbattle.domain.AnswerEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {
    boolean existsByRoundIdAndPlayerId(Long roundId, Long playerId);
    long countByRoundId(Long roundId);
    Optional<AnswerEntity> findFirstByRoundIdAndCorrectnessGreaterThanOrderByResponseMsAsc(Long roundId, double correctness);

    @Query("select a.player.id from AnswerEntity a where a.round.id = :roundId")
    List<Long> findPlayerIdsByRoundId(@Param("roundId") Long roundId);
}
