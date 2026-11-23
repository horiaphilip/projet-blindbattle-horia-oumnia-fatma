package fr.miage.blindbattle.service;

public class SearchTooShort extends RuntimeException {
    public SearchTooShort() { super("La recherche doit contenir au moins 2 caractères"); }
}
