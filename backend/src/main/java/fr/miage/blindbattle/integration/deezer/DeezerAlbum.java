package fr.miage.blindbattle.integration.deezer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeezerAlbum(long id, String title, @JsonProperty("cover_medium") String cover) {}
