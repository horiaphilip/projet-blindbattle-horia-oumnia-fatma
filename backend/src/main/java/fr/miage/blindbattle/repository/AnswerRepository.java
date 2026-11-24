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
    List<AnswerEntity> findByRoundIdAndRejectedReasonIsNull(Long roundId);
    Optional<AnswerEntity> findFirstByRoundIdAndCorrectnessGreaterThanOrderByResponseMsAsc(Long roundId, double correctness);

    @Query("select a.player.id from AnswerEntity a where a.round.id = :roundId")
    List<Long> findPlayerIdsByRoundId(@Param("roundId") Long roundId);

    @Query("select count(a) from AnswerEntity a where a.player.id = :playerId and a.correctness = 1")
    long countCorrectByPlayerId(@Param("playerId") Long playerId);

    @Query("select coalesce(avg(a.responseMs), 0) from AnswerEntity a where a.player.id = :playerId and a.rejectedReason is null")
    double averageResponseMsByPlayerId(@Param("playerId") Long playerId);
}
