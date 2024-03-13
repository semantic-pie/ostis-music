package io.github.semanticpie.orchestrator.orchestrator.agents.waffleWavesAgent;

import io.github.semanticpie.orchestrator.orchestrator.agents.waffleWavesAgent.patterns.ListPattern;
import io.github.semanticpie.orchestrator.orchestrator.agents.waffleWavesAgent.patterns.WaffleWavesPattern;
import io.github.semanticpie.orchestrator.services.CacheService;
import io.github.semanticpie.orchestrator.services.JmanticService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.ostis.api.context.DefaultScContext;
import org.ostis.scmemory.model.element.ScElement;
import org.ostis.scmemory.model.element.edge.EdgeType;
import org.ostis.scmemory.model.element.link.ScLinkString;
import org.ostis.scmemory.model.element.node.NodeType;
import org.ostis.scmemory.model.element.node.ScNode;
import org.ostis.scmemory.model.exception.ScMemoryException;
import org.ostis.scmemory.model.pattern.ScPattern;
import org.ostis.scmemory.websocketmemory.memory.element.ScEdgeImpl;
import org.ostis.scmemory.websocketmemory.memory.element.ScLinkStringImpl;
import org.ostis.scmemory.websocketmemory.memory.element.ScNodeImpl;
import org.ostis.scmemory.websocketmemory.memory.pattern.DefaultWebsocketScPattern;
import org.ostis.scmemory.websocketmemory.memory.pattern.SearchingPatternTriple;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.AliasPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.FixedPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.TypePatternElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class WaffleWavesService {

    private final ScElement next;
    private final ScElement start;
    private final ScElement end;
    private final DefaultScContext context;

    private final CacheService service;
    private final WaffleWavesPattern waffleWavesPattern;

    @Getter
    private final Map<ScElement, Integer> userGenresMap;

    @Autowired
    public WaffleWavesService(JmanticService service, CacheService cacheService, WaffleWavesPattern waffleWavesPattern) {
        this.context = service.getContext();
        this.service = cacheService;
        this.waffleWavesPattern = waffleWavesPattern;
        this.userGenresMap = new HashMap<>();
        this.next = this.service.get("nrel_next", NodeType.CONST_NO_ROLE);
        this.start = this.service.get("concept_start", NodeType.CONST_CLASS);
        this.end = this.service.get("concept_end", NodeType.CONST_CLASS);
    }

    public void loadGenreWeights(ScElement userNode) throws ScMemoryException {

        ScNode nrelWeight = service.get("nrel_weight", NodeType.CONST_NO_ROLE);
        ScNode userGenresTuple = service.get("user_genres", NodeType.CONST_TUPLE);

        var genreList = context.find(waffleWavesPattern.userGenresPattern(userNode, userGenresTuple, nrelWeight)).toList();

        log.info("Genres count: {}", genreList.size());
        log.info("Genres list: {}", genreList);

        for (var genre : genreList) {

            var scElements = genre.filter((element) -> element != null &&
                    (!element.equals(userGenresTuple) && !element.equals(userNode) && !element.equals(nrelWeight))
                    && (element.getClass() == ScLinkStringImpl.class || element.getClass() == ScNodeImpl.class)
            ).toList();

            int value = Integer.parseInt(context.getStringLinkContent((ScLinkString) scElements.get(1))
                    .replace("float:", "").replace("\"", ""));

            userGenresMap.put(scElements.get(0), value);
        }
    }

    public List<ScElement> getAndDeleteOldPlaylist(ScElement playlistNode, ScElement userNode) throws ScMemoryException {
        List<ScElement> tracks = new ArrayList<>();
        List<ScElement> edges = new ArrayList<>();

        context.find(waffleWavesPattern.findPlaylistTrackNodestPattern(userNode, playlistNode)).toList().forEach(stream -> {
            List<ScElement> elements = stream.filter(Objects::nonNull).collect(Collectors.toList());
            tracks.addAll(elements.stream().filter(scElement -> scElement instanceof ScNodeImpl).filter(ScElement -> !ScElement.equals(userNode) && !ScElement.equals(playlistNode)).toList());
            edges.addAll(elements.stream().filter(scElement -> scElement instanceof ScEdgeImpl).toList());
        });

        ScPattern pattern = new DefaultWebsocketScPattern();

        if (!tracks.isEmpty()) {
            for (int i = 0; i < tracks.size() - 1; i++) {
                pattern.addElement(new SearchingPatternTriple(new FixedPatternElement(tracks.get(i)),
                        new TypePatternElement<>(EdgeType.D_COMMON_VAR, new AliasPatternElement("edge_" + i)),
                        new FixedPatternElement(tracks.get(i + 1))));

            }
            edges.addAll(context.find(pattern).toList().get(0).filter(scElement -> scElement.getClass() == ScEdgeImpl.class).toList());

            edges.addAll(getEdgesOfMetaRelation(userNode, tracks, end));

            edges.addAll(getEdgesOfMetaRelation(userNode, tracks, start));

            context.deleteElements(edges.stream());
        }
        return tracks;
    }

    public void uploadPlaylist(ScElement playlistNode, List<ScElement> playlist, ScElement userNode) throws ScMemoryException {
        var tuple = context.memory().generate(waffleWavesPattern.linkToStructurePattern(playlistNode, playlist)).filter(scElement -> !scElement.equals(playlistNode))
                .filter(scElement -> playlist.stream().noneMatch(track -> track.equals(scElement))).toList();

        List<ScElement> list = new ArrayList<>(uploadList(playlist).filter(scElement -> playlist.stream().noneMatch(track -> track.equals(scElement))).toList());
        list.addAll(playlist);
        list.addAll(tuple);
        log.info("Playlist size: {}", playlist.size());
        log.info("Uploaded list size: {}", list.size());
        context.memory().generate(waffleWavesPattern.linkToStructurePattern(userNode, list));
    }

    public List<ScElement> createPlaylist(int size, List<ScElement> oldPlaylist) {
        Random random = new Random();
        int sum = userGenresMap.values().stream().mapToInt(Integer::intValue).sum();

        log.info("sum: {}", sum);

        userGenresMap.replaceAll((key, value) -> Math.round(((float) value / sum) * size));
        userGenresMap.forEach((key, value) -> log.info("limit: {}", value));
        List<ScElement> playlist = new ArrayList<>(Collections.nCopies(size, null));
        userGenresMap.forEach((key, value) -> {
            try {
                AtomicInteger iter = new AtomicInteger();
                getTracksByGenre(key, value, oldPlaylist).forEach((track) -> {
                    while (true) {
                        int index = random.nextInt(size);
                        if (playlist.get(index) == null) {
                            iter.getAndIncrement();
                            playlist.set(index, track);
                            break;
                        }
                    }
                });
                System.out.println("Generate expected: " + value);
                System.out.println("Generate facted: " + iter);


            } catch (ScMemoryException e) {
                log.error(e.getLocalizedMessage());
                throw new RuntimeException(e);
            }
        });
        playlist.removeAll(Collections.singleton(null));
        log.info("Playlist size: {}", playlist.size());
        log.info("Playlist: {}", playlist);

        return playlist;
    }

    private List<ScElement> getEdgesOfMetaRelation(ScElement userNode, List<ScElement> tracks, ScElement metaRelation) throws ScMemoryException {

        List<ScElement> edges = new ArrayList<>();
        for (var relations : context.find(waffleWavesPattern.scPattern5ForPlaylist(userNode, metaRelation)).toList()) {
            List<ScElement> temp = relations.collect(Collectors.toList());
            if (temp.stream().anyMatch(tracks::contains)) {
                edges.addAll(temp.stream().filter(scElement -> scElement instanceof ScEdgeImpl).toList());
            }
        }
        return edges;
    }

    private Stream<? extends ScElement> uploadList(List<ScElement> list) throws ScMemoryException {
        return context.memory().generate(new ListPattern(list, start, next, end).getPattern()).filter(Objects::nonNull).filter(scElement -> !scElement.equals(end) && !scElement.equals(start) && !scElement.equals(next));
    }


    private List<ScElement> getTracksByGenre(ScElement genreNode, int limit, List<ScElement> oldPlaylist) throws ScMemoryException {
        ScNode conceptTrack = this.service.get("concept_track", NodeType.CONST_CLASS);
        ScNode nrelGenre = this.service.get("nrel_genre", NodeType.CONST_NO_ROLE);

        List<ScElement> output = new ArrayList<>();
        var trackList = context.find(waffleWavesPattern.trackPattern(genreNode, conceptTrack, nrelGenre)).toList();
        int index = 0;
        for (var track : trackList) {
            if (index >= limit) break;

            ScElement element = track.filter(Objects::nonNull).filter(scElement -> !scElement.equals(genreNode) && !scElement.equals(conceptTrack))
                    .filter(scElement -> scElement.getClass() != ScEdgeImpl.class).findFirst().orElseThrow();
            if (!oldPlaylist.contains(element)) {
                index++;
                output.add(element);
            }
        }
        return output;
    }
}

