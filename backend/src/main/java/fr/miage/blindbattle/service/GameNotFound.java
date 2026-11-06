package fr.miage.blindbattle.service;

public class GameNotFound extends RuntimeException {
    public GameNotFound(String code) { super("Partie " + code + " introuvable"); }
}
