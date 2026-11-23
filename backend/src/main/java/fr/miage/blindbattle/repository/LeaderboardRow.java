package fr.miage.blindbattle.repository;

public interface LeaderboardRow {
    long getUserId();
    String getName();
    long getGamesPlayed();
    long getTotalScore();
    int getBestScore();
    long getWins();
}
