package fr.miage.blindbattle.repository;

import java.time.Instant;

public interface HistoryRow {
    String getGameCode();
    Instant getPlayedAt();
    int getRank();
    int getFinalScore();
    int getCorrectAnswers();
    int getTotalRounds();
    long getPlayerCount();
}
