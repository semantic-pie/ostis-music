package io.github.semanticpie.orchestrator.orchestrator.agents.trackAgent.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackData {
    private String hash;
    private String title;
    private String album;
    private String artist;
    private String genre;
    private String releaseYear;
    private String trackNumber;
    private Integer bitrate;
    private Long lengthInMilliseconds;
}
