package fr.miage.blindbattle.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ThemeCatalog {
    public static final String WORLD_CHART = "chart-world";
    public static final String ARTIST_PREFIX = "artist-";

    private final List<Theme> themes = List.of(
            new Theme(WORLD_CHART, "Top Monde", "Classements", 0),
            new Theme("chart-fr", "Top France", "Classements", 1109890291L),
            new Theme("genre-rap", "Rap", "Genres", 1071669561L),
            new Theme("genre-rock", "Rock", "Genres", 3126664682L),
            new Theme("genre-pop", "Pop", "Genres", 1036183001L),
            new Theme("genre-electro", "Électro", "Genres", 11233152384L),
            new Theme("decade-80", "Années 80", "Décennies", 8788889882L),
            new Theme("decade-90", "Années 90", "Décennies", 878989033L),
            new Theme("decade-2000", "Années 2000", "Décennies", 1977689462L));

    public List<Theme> all() { return themes; }

    public Optional<Theme> find(String id) { return themes.stream().filter(theme -> theme.id().equals(id)).findFirst(); }
}
