package io.github.semanticpie.orchestrator.orchestrator.agents.trackAgent.services;

import io.github.semanticpie.orchestrator.orchestrator.agents.trackAgent.models.TrackData;

public interface TrackService {
    void uploadTrackToScMachine(TrackData trackDataDTO);
}
