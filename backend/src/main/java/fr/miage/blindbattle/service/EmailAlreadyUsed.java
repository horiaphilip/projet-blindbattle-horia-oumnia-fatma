package fr.miage.blindbattle.service;

public class EmailAlreadyUsed extends RuntimeException {
    public EmailAlreadyUsed(String email) { super("L'email " + email + " est déjà utilisé"); }
}
