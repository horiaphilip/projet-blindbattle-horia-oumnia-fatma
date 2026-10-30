package fr.miage.blindbattle.service;

public class StatsNotReady extends RuntimeException {
    public StatsNotReady(long userId) { super("Les statistiques de l'utilisateur " + userId + " ne sont pas encore disponibles"); }
}
