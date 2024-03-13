package io.github.semanticpie.derezhor.externalAgents.findTracks.api;

import io.github.semanticpie.derezhor.externalAgents.findTracks.models.GenreDTO;
import io.github.semanticpie.derezhor.externalAgents.findTracks.models.TrackDTO;
import io.github.semanticpie.derezhor.externalAgents.findTracks.services.FindTracksService;
import io.github.semanticpie.derezhor.externalAgents.findTracks.services.LikeService;
import io.github.semanticpie.derezhor.externalAgents.users.services.UserService;
import io.github.semanticpie.derezhor.externalAgents.users.services.utils.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/derezhor/tracks")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PATCH, RequestMethod.OPTIONS})
public class TrackController {

    private final FindTracksService findTracksService;
    private final LikeService likeService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping()
    public List<TrackDTO> findAll(@RequestParam("page") Integer page, @RequestParam("limit") Integer limit, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String hash = null;

        if (token != null && !token.isEmpty()) {
            String jwtToken = token.substring(7);
            hash = jwtTokenProvider.getUUID(jwtToken);
        }

        return findTracksService.findAll(page, limit, hash);
    }

    @PostMapping("/{hash}/like")
    public ResponseEntity<?> likeTrack(@PathVariable String hash, HttpServletRequest request) {
        return likeService.likeTrack(hash, request);
    }


    @PostMapping("/{hash}/dislike")
    public ResponseEntity<?> dislikeTrack(@PathVariable String hash, HttpServletRequest request) {
        return likeService.dislikeTrack(hash, request);
    }
    
    @PostMapping("/playlist/generate")
    public ResponseEntity<?> generatePlaylist(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String hash = null;

        if (token != null && !token.isEmpty()) {
            String jwtToken = token.substring(7);
            hash = jwtTokenProvider.getUUID(jwtToken);
            findTracksService.generatePlaylist(hash);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/genres")
    public List<GenreDTO> getAllGenres() {
        return findTracksService.getGenres();
    }


    @CrossOrigin("*")
    @GetMapping("/{name}/playlist")
    public List<TrackDTO> findByPlaylist(@PathVariable("name") String name, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String hash = null;

        if (token != null && !token.isEmpty()) {
            String jwtToken = token.substring(7);
            hash = jwtTokenProvider.getUUID(jwtToken);
            return findTracksService.findByPlaylist(hash, name);
        }

        throw new RuntimeException("Failed get playlist");
    }

}
