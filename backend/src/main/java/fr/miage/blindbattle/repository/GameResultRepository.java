package fr.miage.blindbattle.repository;

import fr.miage.blindbattle.domain.GameResultEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameResultRepository extends JpaRepository<GameResultEntity, Long> {
    boolean existsByGameIdAndPlayerId(Long gameId, Long playerId);

    @Query("select r.rank as rank, p.nickname as nickname, r.finalScore as finalScore, r.correctAnswers as correctAnswers, "
            + "r.avgResponseMs as avgResponseMs from GameResultEntity r join r.player p where r.game.code = :code order by r.rank, p.nickname")
    List<PodiumRow> findPodium(@Param("code") String code);

    @Query(value = "select u.id as userId, u.name as name, count(r) as gamesPlayed, sum(r.finalScore) as totalScore, "
            + "max(r.finalScore) as bestScore, sum(case when r.rank = 1 then 1 else 0 end) as wins "
            + "from GameResultEntity r join r.player p join p.user u group by u.id, u.name order by sum(r.finalScore) desc, u.name",
            countQuery = "select count(distinct p.user.id) from GameResultEntity r join r.player p where p.user is not null")
    Page<LeaderboardRow> findLeaderboard(Pageable pageable);

    @Query(value = "select g.code as gameCode, r.createdAt as playedAt, r.rank as rank, r.finalScore as finalScore, "
            + "r.correctAnswers as correctAnswers, g.settings.roundCount as totalRounds, "
            + "(select count(o) from GameResultEntity o where o.game = g) as playerCount "
            + "from GameResultEntity r join r.game g join r.player p where p.user.id = :userId order by r.createdAt desc",
            countQuery = "select count(r) from GameResultEntity r where r.player.user.id = :userId")
    Page<HistoryRow> findHistory(@Param("userId") long userId, Pageable pageable);
}
