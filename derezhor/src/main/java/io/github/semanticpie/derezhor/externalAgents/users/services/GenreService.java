package io.github.semanticpie.derezhor.externalAgents.users.services;


import io.github.semanticpie.derezhor.externalAgents.users.dtos.GenreDTO;

import java.util.List;

public interface GenreService {
    void addGenres(String user, List<GenreDTO> genreDTOS);
}
