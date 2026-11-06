package fr.miage.blindbattle.service;

public class NicknameTaken extends RuntimeException {
    public NicknameTaken(String nickname) { super("Le pseudo " + nickname + " est déjà pris dans cette partie"); }
}
