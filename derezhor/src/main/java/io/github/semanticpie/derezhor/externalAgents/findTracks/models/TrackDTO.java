package io.github.semanticpie.derezhor.externalAgents.findTracks.models;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class TrackDTO {
    private Long scAddr;
    private String hash;
    private String title;
    private String author;
    private GenreDTO genre;
    private Boolean liked;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackDTO trackDTO = (TrackDTO) o;
        return Objects.equals(hash, trackDTO.hash) && Objects.equals(title, trackDTO.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, title);
    }
}
