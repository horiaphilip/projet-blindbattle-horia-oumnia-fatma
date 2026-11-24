package fr.miage.blindbattle.integration.deezer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeezerArtist(long id, String name, @JsonProperty("picture_medium") String picture) {}
