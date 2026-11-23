package fr.miage.blindbattle.repository;

public interface PodiumRow {
    int getRank();
    String getNickname();
    int getFinalScore();
    int getCorrectAnswers();
    long getAvgResponseMs();
}
