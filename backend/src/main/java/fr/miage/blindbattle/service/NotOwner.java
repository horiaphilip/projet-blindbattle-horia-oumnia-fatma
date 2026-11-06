package fr.miage.blindbattle.service;

public class NotOwner extends RuntimeException {
    public NotOwner() { super("Action réservée au propriétaire"); }
}
