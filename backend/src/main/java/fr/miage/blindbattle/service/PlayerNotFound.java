package fr.miage.blindbattle.service;

public class PlayerNotFound extends RuntimeException {
    public PlayerNotFound() { super("Joueur introuvable dans cette partie"); }
}
