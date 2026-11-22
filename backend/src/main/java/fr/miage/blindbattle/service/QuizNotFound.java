package fr.miage.blindbattle.service;

public class QuizNotFound extends RuntimeException {
    public QuizNotFound(long id) { super("Quiz " + id + " introuvable"); }

    public QuizNotFound() { super("Quiz introuvable"); }
}
