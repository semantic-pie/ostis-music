package io.github.semanticpie.derezhor.externalAgents.findTracks.services.impl;

import io.github.semanticpie.derezhor.externalAgents.findTracks.services.LikeService;
import io.github.semanticpie.derezhor.externalAgents.users.services.UserService;
import io.github.semanticpie.derezhor.externalAgents.users.services.utils.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ostis.api.context.DefaultScContext;
import org.ostis.scmemory.model.element.edge.EdgeType;
import org.ostis.scmemory.model.element.edge.ScEdge;
import org.ostis.scmemory.model.element.node.NodeType;
import org.ostis.scmemory.model.exception.ScMemoryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@Slf4j
@AllArgsConstructor
public class LikeServiceImpl implements LikeService {

    private DefaultScContext context;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public ResponseEntity<?> likeTrack(String trackHash, HttpServletRequest request) {
        try {
            String jwtToken = request.getHeader("Authorization").substring(7);
            var username = jwtTokenProvider.getUsername(jwtToken);
            var uuid = jwtTokenProvider.getUUID(jwtToken);

            if (isUsernameEqualsUUID(uuid, username)) {
                var nrelLike = context.resolveKeynode("nrel_likes", NodeType.CONST_NO_ROLE);
                var userNode = context.findKeynode(uuid).orElseThrow();
                var trackNode = context.findKeynode(trackHash).orElseThrow();
                ScEdge userTrackEdge = context.resolveEdge(userNode, EdgeType.D_COMMON_VAR, trackNode);
                context.createEdge(EdgeType.ACCESS_CONST_POS_PERM, nrelLike, userTrackEdge);
            }
        } catch (ScMemoryException ex) {
            log.error("Can't like track");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> dislikeTrack(String trackHash, HttpServletRequest request) {
        try {
            String jwtToken = request.getHeader("Authorization").substring(7);
            var username = jwtTokenProvider.getUsername(jwtToken);
            var uuid = jwtTokenProvider.getUUID(jwtToken);

            if (isUsernameEqualsUUID(uuid, username)) {
                var unlike = context.resolveKeynode("unlike", NodeType.CONST_CLASS);
                var userNode = context.findKeynode(uuid).orElseThrow();
                var trackNode = context.findKeynode(trackHash).orElseThrow();
                ScEdge userTrackEdge = context.resolveEdge(userNode, EdgeType.D_COMMON_VAR, trackNode);
                context.createEdge(EdgeType.ACCESS_CONST_POS_PERM, unlike, userTrackEdge);
            }
        } catch (ScMemoryException ex) {
            log.error("Can't dislike track");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean isUsernameEqualsUUID(String userUUID, String username) {
        try {
            var uuidByUsername = userService.getUserUUID(username).orElseThrow();
            return userUUID.equals(uuidByUsername);
        } catch (NoSuchElementException ex) {
            return false;
        }
    }
}
