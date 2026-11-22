package fr.miage.blindbattle.service;

public class TrackNotFound extends RuntimeException {
    public TrackNotFound(long id) { super("Morceau " + id + " introuvable"); }
}
