package io.github.semanticpie.derezhor.externalAgents.findTracks.models;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class GenreDTO {
    String idtf;
    String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenreDTO genreDTO = (GenreDTO) o;
        return Objects.equals(idtf, genreDTO.idtf) && Objects.equals(name, genreDTO.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idtf, name);
    }
}
