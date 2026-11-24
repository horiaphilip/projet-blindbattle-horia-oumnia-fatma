package fr.miage.blindbattle.service;

public class GameSourceMissing extends RuntimeException {
    public GameSourceMissing() { super("Choisissez un thème ou un quiz"); }
}
