package fr.miage.blindbattle.messaging.pipeline.rules;

import fr.miage.blindbattle.domain.GameStatus;
import fr.miage.blindbattle.messaging.pipeline.AnswerRule;
import fr.miage.blindbattle.messaging.pipeline.SubmitAnswer;
import fr.miage.blindbattle.repository.GameRepository;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class GameNotPlayingRule implements AnswerRule {
    private final GameRepository games;

    public GameNotPlayingRule(GameRepository games) { this.games = games; }

    @Override
    public Optional<String> reject(SubmitAnswer answer) {
        boolean playing = games.findByCode(answer.gameCode()).map(g -> g.getStatus() == GameStatus.PLAYING).orElse(false);
        return playing ? Optional.empty() : Optional.of("La partie n'est pas en cours");
    }
}
