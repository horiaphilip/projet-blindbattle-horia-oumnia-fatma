package fr.miage.blindbattle.service;

public class ThemeNotFound extends RuntimeException {
    public ThemeNotFound(String id) { super("Thème " + id + " introuvable"); }
}
