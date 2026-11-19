package fr.miage.blindbattle.game;

import org.springframework.stereotype.Component;

@Component
public class Levenshtein {
    public int distance(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) previous[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int substitution = previous[j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1);
                current[j] = Math.min(substitution, Math.min(previous[j], current[j - 1]) + 1);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }

    public boolean close(String answer, String expected) {
        if (answer.isEmpty() || expected.isEmpty()) return false;
        return distance(answer, expected) <= Math.max(1, Math.round(expected.length() * 0.2f));
    }
}
