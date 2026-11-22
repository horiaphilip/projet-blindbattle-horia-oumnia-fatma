package fr.miage.blindbattle.messaging.pipeline;

import fr.miage.blindbattle.domain.GameSettings;
import fr.miage.blindbattle.domain.RoundEntity;
import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.repository.PlayerRepository;
import fr.miage.blindbattle.repository.RoundRepository;
import org.springframework.stereotype.Component;

@Component
public class AnswerEnricher {
    private final RoundRepository rounds;
    private final PlayerRepository players;

    public AnswerEnricher(RoundRepository rounds, PlayerRepository players) {
        this.rounds = rounds;
        this.players = players;
    }

    public EnrichedAnswer enrich(SubmitAnswer answer) {
        RoundEntity round = rounds.findById(answer.roundId()).orElseThrow();
        TrackEntity track = round.getTrack();
        GameSettings settings = round.getGame().getSettings();
        int streak = players.findById(answer.playerId()).orElseThrow().getStreak();
        return new EnrichedAnswer(answer, track.getTitle(), track.getArtist(), round.getChoiceTrackIds(),
                round.getChoiceTrackIds().indexOf(track.getId()), round.getStartedAt(), settings.roundSeconds(),
                settings.guessTarget(), streak);
    }
}
