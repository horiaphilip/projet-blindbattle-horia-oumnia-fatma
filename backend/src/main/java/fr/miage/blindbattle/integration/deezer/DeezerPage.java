package fr.miage.blindbattle.integration.deezer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeezerPage<T>(List<T> data, Integer total, String next) {}
