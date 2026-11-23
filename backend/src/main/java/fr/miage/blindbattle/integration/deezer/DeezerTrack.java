package fr.miage.blindbattle.integration.deezer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeezerTrack(Long id, String title, String preview, Integer rank, Integer duration, DeezerArtist artist,
        DeezerAlbum album) {}
