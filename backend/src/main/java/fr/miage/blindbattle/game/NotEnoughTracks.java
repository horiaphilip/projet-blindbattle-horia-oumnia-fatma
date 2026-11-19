package fr.miage.blindbattle.game;

public class NotEnoughTracks extends RuntimeException {
    public NotEnoughTracks() { super("Pas assez de morceaux différents pour proposer quatre réponses"); }
}
