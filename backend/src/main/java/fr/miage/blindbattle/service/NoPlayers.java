package fr.miage.blindbattle.service;

public class NoPlayers extends RuntimeException {
    public NoPlayers(String code) { super("Aucun joueur n'a rejoint la partie " + code); }
}
