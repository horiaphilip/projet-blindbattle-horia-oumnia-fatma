package fr.miage.blindbattle.service;

public class NoRoundInProgress extends RuntimeException {
    public NoRoundInProgress(String code) { super("Aucune manche n'a commencé dans la partie " + code); }
}
