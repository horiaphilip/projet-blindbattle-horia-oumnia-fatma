package fr.miage.blindbattle.service;

public class GameAlreadyStarted extends RuntimeException {
    public GameAlreadyStarted(String code) { super("La partie " + code + " a déjà commencé"); }
}
