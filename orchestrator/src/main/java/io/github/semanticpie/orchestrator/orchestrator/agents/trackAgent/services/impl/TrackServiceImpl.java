package io.github.semanticpie.orchestrator.orchestrator.agents.trackAgent.services.impl;

import io.github.semanticpie.orchestrator.orchestrator.agents.trackAgent.models.TrackData;
import io.github.semanticpie.orchestrator.orchestrator.agents.trackAgent.services.TrackService;
import io.github.semanticpie.orchestrator.services.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.ostis.api.context.DefaultScContext;
import org.ostis.scmemory.model.element.ScElement;
import org.ostis.scmemory.model.element.edge.EdgeType;
import org.ostis.scmemory.model.element.edge.ScEdge;
import org.ostis.scmemory.model.element.link.LinkType;
import org.ostis.scmemory.model.element.link.ScLink;
import org.ostis.scmemory.model.element.node.NodeType;
import org.ostis.scmemory.model.element.node.ScNode;
import org.ostis.scmemory.model.exception.ScMemoryException;
import org.ostis.scmemory.model.pattern.ScPattern;
import org.ostis.scmemory.websocketmemory.memory.pattern.DefaultWebsocketScPattern;
import org.ostis.scmemory.websocketmemory.memory.pattern.SearchingPatternTriple;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.AliasPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.FixedPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.TypePatternElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class TrackServiceImpl implements TrackService {

    private final DefaultScContext context;

    private final ScNode conceptTrack;
    private final ScNode conceptArtist;
    private final ScNode nrelIdtf;
    private final ScNode nrelArtist;
    private final ScNode langEn;

    private final ScNode nrelGenre;

    @Autowired
    public TrackServiceImpl(CacheService cache, DefaultScContext context) {
        this.context = context;
        this.conceptTrack = cache.get("concept_track");
        this.conceptArtist = cache.get("concept_artist");
        this.nrelIdtf = cache.get("nrel_main_idtf");
        this.langEn = cache.get("lang_en");
        this.nrelArtist = cache.get("nrel_artis");
        this.nrelGenre = cache.get("nrel_genre");
    }

    @Override
    public void uploadTrackToScMachine(TrackData trackData) {
        try {
            // Big ball of mud
            ScNode uploadedTrack = context.resolveKeynode(trackData.getHash(), NodeType.CONST);
            context.resolveEdge(conceptTrack, EdgeType.ACCESS_VAR_POS_PERM, uploadedTrack);
            ScLink trackName = context.createStringLink(LinkType.LINK_CONST, trackData.getTitle());
            context.resolveEdge(langEn, EdgeType.ACCESS_VAR_POS_PERM, trackName);
            ScEdge edge = context.resolveEdge(uploadedTrack, EdgeType.D_COMMON_VAR, trackName);
            context.resolveEdge(nrelIdtf, EdgeType.ACCESS_VAR_POS_PERM, edge);
            ScElement artist = resolveArtist(trackData.getArtist());
            edge = context.resolveEdge(uploadedTrack, EdgeType.D_COMMON_VAR, artist);
            context.resolveEdge(nrelArtist, EdgeType.ACCESS_VAR_POS_PERM, edge);

            List<ScNode> genreNodes = resolveGenres(Arrays.stream(trackData.getGenre().split("/")).toList());
            ScNode genreGeneralNode = context.resolveKeynode("concept_music_genre", NodeType.CONST_CLASS);

            genreNodes.forEach(genre -> {
                try {
                    context.resolveEdge(genreGeneralNode, EdgeType.ACCESS_VAR_POS_PERM,genre);
                } catch (ScMemoryException e) {
                    log.error(e.getLocalizedMessage());
                }
            });

            List<ScElement> edges = context.createEdges(
                    Stream.generate(() -> EdgeType.D_COMMON_CONST).limit(genreNodes.size()),
                    Stream.generate(() -> uploadedTrack).limit(genreNodes.size()),
                    genreNodes.stream()
                    ).collect(Collectors.toList());
            context.createEdges(
                    Stream.generate(() -> EdgeType.ACCESS_CONST_POS_PERM).limit(edges.size()),
                    Stream.generate(() -> nrelGenre).limit(edges.size()),
                    edges.stream());
        } catch (ScMemoryException ex) {
            throw new TrackServiceException(ex);
        }

    }

    private String conceptGenre(String genreName) {
        return "concept_" + genreName.toLowerCase().replace(" ", "_").replace("-", "_");
    }

    private ScPattern artistFindPattern(ScElement artistName) {
        ScPattern pattern = new DefaultWebsocketScPattern();
        pattern.addElement(new SearchingPatternTriple(
                new FixedPatternElement(conceptArtist),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("_edge1")),
                new TypePatternElement<>(NodeType.VAR, new AliasPatternElement("_name"))));
        pattern.addElement(new SearchingPatternTriple(
                new TypePatternElement<>(NodeType.VAR, new AliasPatternElement("_name")),
                new TypePatternElement<>(EdgeType.D_COMMON_VAR, new AliasPatternElement("_edge2")),
                new FixedPatternElement(artistName)));
        pattern.addElement(new SearchingPatternTriple(
                new FixedPatternElement(nrelIdtf),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("_edge3")),
                new TypePatternElement<>(EdgeType.D_COMMON_VAR, new AliasPatternElement("_edge2"))));
        return pattern;
    }

    private ScElement resolveArtist(String trackArtist) throws ScMemoryException {
        try {
            ScElement artistName = context.findByName(trackArtist).orElseThrow(ScMemoryException::new);
            var result = context.find(artistFindPattern(artistName))
                    .findFirst().orElseThrow(ScMemoryException::new).toList().get(2);
            log.info("artist [{}] found", trackArtist);
            return result;
        } catch (ScMemoryException | IndexOutOfBoundsException ex) {
            log.info("artist [{}] NOT founded, creation", trackArtist);
            return createArtist(trackArtist);
        }
    }

    private ScElement createArtist(String trackArtist) throws ScMemoryException {
        ScNode artist = context.resolveKeynode(UUID.randomUUID().toString(), NodeType.CONST);
        ScLink artistName = context.createStringLink(LinkType.LINK_CONST, trackArtist);
        ScEdge edgeName = context.resolveEdge(artist, EdgeType.D_COMMON_VAR, artistName);

        context.resolveEdge(langEn, EdgeType.ACCESS_VAR_POS_PERM, artistName);
        context.resolveEdge(conceptArtist, EdgeType.ACCESS_VAR_POS_PERM, artist);
        context.resolveEdge(nrelIdtf, EdgeType.ACCESS_VAR_POS_PERM, edgeName);
        return artist;
    }

    private ScNode resolveGenre(String genre) throws ScMemoryException {
        return context.resolveKeynode(genre, NodeType.CONST_CLASS);
    }

    private List<ScNode> resolveGenres(List<String> genres) throws ScMemoryException {
        return genres.stream().map(genre -> {
            try {
                return resolveGenre(conceptGenre(genre));
            } catch (ScMemoryException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }
}
