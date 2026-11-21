package fr.miage.blindbattle.service;

public class NotQuizOwner extends RuntimeException {
    public NotQuizOwner() { super("Ce quiz appartient à un autre joueur"); }
}
