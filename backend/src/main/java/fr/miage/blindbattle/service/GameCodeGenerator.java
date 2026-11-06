package fr.miage.blindbattle.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class GameCodeGenerator {
    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    public String next() {
        StringBuilder code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        return code.toString();
    }
}
