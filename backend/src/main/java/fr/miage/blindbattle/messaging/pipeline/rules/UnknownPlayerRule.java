package fr.miage.blindbattle.messaging.pipeline.rules;

import fr.miage.blindbattle.messaging.pipeline.AnswerRule;
import fr.miage.blindbattle.messaging.pipeline.SubmitAnswer;
import fr.miage.blindbattle.repository.PlayerRepository;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class UnknownPlayerRule implements AnswerRule {
    private final PlayerRepository players;

    public UnknownPlayerRule(PlayerRepository players) { this.players = players; }

    @Override
    public Optional<String> reject(SubmitAnswer answer) {
        boolean inGame = players.findById(answer.playerId())
                .map(p -> p.getGame().getCode().equals(answer.gameCode())).orElse(false);
        return inGame ? Optional.empty() : Optional.of("Joueur inconnu dans cette partie");
    }
}
