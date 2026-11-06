package fr.miage.blindbattle.service;

public class NotHost extends RuntimeException {
    public NotHost() { super("Action réservée à l'hôte de la partie"); }
}
