package fr.miage.blindbattle.repository;

import fr.miage.blindbattle.domain.BadgeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<BadgeEntity, Long> {
    List<BadgeEntity> findByUserIdOrderByEarnedAtAsc(Long userId);
}
