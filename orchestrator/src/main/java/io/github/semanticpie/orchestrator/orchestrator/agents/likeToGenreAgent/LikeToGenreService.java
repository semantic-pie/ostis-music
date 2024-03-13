package io.github.semanticpie.orchestrator.orchestrator.agents.likeToGenreAgent;

import io.github.semanticpie.orchestrator.orchestrator.agents.waffleWavesAgent.patterns.WaffleWavesPattern;
import io.github.semanticpie.orchestrator.services.CacheService;
import io.github.semanticpie.orchestrator.services.JmanticService;
import lombok.AllArgsConstructor;
import org.ostis.api.context.DefaultScContext;
import org.ostis.scmemory.model.element.ScElement;
import org.ostis.scmemory.model.element.edge.EdgeType;
import org.ostis.scmemory.model.element.edge.ScEdge;
import org.ostis.scmemory.model.element.link.LinkType;
import org.ostis.scmemory.model.element.link.ScLink;
import org.ostis.scmemory.model.element.link.ScLinkString;
import org.ostis.scmemory.model.element.node.NodeType;
import org.ostis.scmemory.model.element.node.ScNode;
import org.ostis.scmemory.model.exception.ScMemoryException;
import org.ostis.scmemory.websocketmemory.memory.element.ScLinkStringImpl;
import org.ostis.scmemory.websocketmemory.memory.element.ScNodeImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class LikeToGenreService {
    private final DefaultScContext context;

    private final LikeToGenrePatterns patterns;

    private final CacheService cacheService;

    @Autowired
    public LikeToGenreService(JmanticService  jmanticService, LikeToGenrePatterns patterns, CacheService cacheService){
        this.context = jmanticService.getContext();
        this.patterns = patterns;
        this.cacheService =  cacheService;
    }

    public ScElement getGenre(ScElement trackNode) throws ScMemoryException {
        ScElement conceptGenre = cacheService.get("nrel_genre");

        return context.find(patterns.patternOfSearchingTrackGenre(trackNode, conceptGenre))
                .findFirst().orElseThrow()
                .filter(scElement -> scElement instanceof ScNodeImpl)
                .filter(scElement -> !scElement.equals(trackNode))
                .filter(scElement -> !scElement.equals(conceptGenre)).findFirst().orElseThrow();
    }

    public void incrementator(ScElement target, int delta) throws ScMemoryException {
        var elements = context.find(patterns.patternOfSearchingNodes(target, NodeType.VAR_STRUCT, NodeType.VAR)).toList().get(0).toList();

        ScElement userNode = null;
        ScElement trackNode = null;

        for (ScElement element : elements) {
            if (element instanceof ScNodeImpl) {
                switch (((ScNodeImpl) element).getType()) {
                    case VAR -> trackNode = element;
                    case VAR_STRUCT -> userNode = element;
                }
            }
        }

        if (userNode != null && trackNode != null) {
            ScElement genre = getGenre(trackNode);
            increment(delta, userNode, genre);
        } else {
            throw new NullPointerException();
        }
    }

    private void increment(int delta, ScElement userNode, ScElement genre) throws ScMemoryException {

        ScNode nrelWeight = cacheService.get("nrel_weight");
        ScNode userGenresTuple = cacheService.get("user_genres");
        WaffleWavesPattern pattern = new WaffleWavesPattern();

        var genreList = context.find(pattern.userGenresPattern(userNode, userGenresTuple, nrelWeight)).toList();

        boolean finded = false;

        for (var genreNode : genreList) {
            var scElements = genreNode.filter((element) -> element != null &&
                    (!element.equals(userGenresTuple) && !element.equals(userNode) && !element.equals(nrelWeight))
                    && (element.getClass() == ScLinkStringImpl.class || element.getClass() == ScNodeImpl.class)
            ).toList();

            if (scElements.get(0).equals(genre)) {
                int value = Integer.parseInt(context.getStringLinkContent((ScLinkString) scElements.get(1))
                        .replace("float:", "").replace("\"", ""));
                context.setStringLinkContent((ScLinkString) scElements.get(1), String.valueOf(value + delta));
                finded = true;
                break;
            }

        }

        if (!finded) {

            ScLink weightNode = context.createStringLink(LinkType.LINK_CONST, Integer.toString(1));
            ScEdge weightRelationEdge = context.resolveEdge(genre, EdgeType.D_COMMON_VAR, weightNode);

            ScEdge genreNodeGenreConceptEdge = context.resolveEdge(userGenresTuple, EdgeType.ACCESS_VAR_POS_PERM, genre);

            ScEdge weightRelationWeightRelationEdge = context.resolveEdge(nrelWeight, EdgeType.ACCESS_VAR_POS_PERM, weightRelationEdge);

            context.memory().generate(pattern.linkToStructurePattern(userNode, List.of(weightNode, weightRelationEdge, genreNodeGenreConceptEdge, weightRelationWeightRelationEdge,
                    genre)));

        }
    }
}
