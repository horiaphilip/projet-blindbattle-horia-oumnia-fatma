package fr.miage.blindbattle.game;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class AnswerNormalizer {
    private static final Pattern MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern BRACKETS = Pattern.compile("\\([^)]*\\)|\\[[^]]*]");
    private static final Pattern VERSION = Pattern.compile("\\s-\\s.*\\b(remaster|remastered|edit|live|version|mix|mono|stereo)\\b.*$");
    private static final Pattern FEATURING = Pattern.compile("\\b(feat\\.?|ft\\.?|featuring)(\\s.*)?$");
    private static final Pattern REMASTERED = Pattern.compile("\\b(\\d{4}\\s)?(remastered|remaster)(\\s\\d{4})?\\b");
    private static final Pattern PUNCTUATION = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final Pattern LEADING_THE = Pattern.compile("^the\\s+");

    public String normalize(String raw) {
        if (raw == null) return "";
        String text = Normalizer.normalize(raw.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        text = MARKS.matcher(text).replaceAll("");
        text = BRACKETS.matcher(text).replaceAll(" ");
        text = VERSION.matcher(text).replaceAll("");
        text = FEATURING.matcher(text).replaceAll("");
        text = REMASTERED.matcher(text).replaceAll(" ");
        text = PUNCTUATION.matcher(text).replaceAll(" ").strip();
        return LEADING_THE.matcher(text).replaceFirst("");
    }
}
