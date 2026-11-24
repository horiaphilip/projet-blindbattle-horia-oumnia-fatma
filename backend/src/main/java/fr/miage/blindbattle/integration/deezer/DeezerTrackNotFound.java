package fr.miage.blindbattle.integration.deezer;

public class DeezerTrackNotFound extends RuntimeException {
    public DeezerTrackNotFound(long deezerId) { super("Morceau Deezer " + deezerId + " introuvable"); }
}
