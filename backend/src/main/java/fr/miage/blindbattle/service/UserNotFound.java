package fr.miage.blindbattle.service;

public class UserNotFound extends RuntimeException {
    public UserNotFound(long id) { super("Utilisateur " + id + " introuvable"); }
}
