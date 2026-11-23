package fr.miage.blindbattle.integration.deezer;

public class DeezerUnavailable extends RuntimeException {
    public DeezerUnavailable(Throwable cause) { super("Deezer est indisponible pour le moment", cause); }
}
