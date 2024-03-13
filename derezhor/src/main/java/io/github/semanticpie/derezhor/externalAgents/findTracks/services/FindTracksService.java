package io.github.semanticpie.derezhor.externalAgents.findTracks.services;

import io.github.semanticpie.derezhor.externalAgents.findTracks.models.GenreDTO;
import io.github.semanticpie.derezhor.externalAgents.findTracks.models.TrackDTO;

import java.util.List;

public interface FindTracksService {
    List<TrackDTO> findAll(Integer page, Integer limit, String userHash);
    List<TrackDTO> findByName(String name);
    List<TrackDTO> findByPlaylist(String user, String playlist);
    void generatePlaylist(String hash);
    List<GenreDTO> getGenres();
}
