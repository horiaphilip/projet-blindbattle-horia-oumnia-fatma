package fr.miage.blindbattle.service;

public class InvalidCredentials extends RuntimeException {
    public InvalidCredentials() { super("Email ou mot de passe incorrect"); }
}
