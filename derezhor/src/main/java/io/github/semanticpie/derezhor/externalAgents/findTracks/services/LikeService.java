package io.github.semanticpie.derezhor.externalAgents.findTracks.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface LikeService {

    ResponseEntity<?> likeTrack(@PathVariable String trackHash, HttpServletRequest request);
    ResponseEntity<?> dislikeTrack(@PathVariable String trackHash, HttpServletRequest request);
}
