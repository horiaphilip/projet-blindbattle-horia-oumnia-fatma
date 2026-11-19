package fr.miage.blindbattle.game;

import org.springframework.stereotype.Component;

@Component
public class ScoringService {
    private static final int MAX_POINTS = 1000;
    private static final int STREAK_STEP = 100;
    private static final int MAX_BONUS = 300;

    public Score score(double correctness, long responseMs, long roundMs, int previousStreak) {
        double elapsed = (double) Math.clamp(responseMs, 0L, roundMs) / roundMs;
        int points = correctness > 0 ? (int) Math.round(MAX_POINTS * (1 - 0.5 * elapsed) * correctness) : 0;
        int streak = correctness == 1 ? previousStreak + 1 : 0;
        int bonus = streak > 0 ? Math.min(STREAK_STEP * (streak - 1), MAX_BONUS) : 0;
        return new Score(points, bonus, points + bonus, streak);
    }
}
