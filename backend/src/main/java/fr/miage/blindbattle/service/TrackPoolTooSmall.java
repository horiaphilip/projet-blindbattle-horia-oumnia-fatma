package fr.miage.blindbattle.service;

public class TrackPoolTooSmall extends RuntimeException {
    public TrackPoolTooSmall(int available, int needed) {
        super("Pas assez de morceaux pour cette partie : " + available + " disponibles, " + needed + " nécessaires");
    }
}
