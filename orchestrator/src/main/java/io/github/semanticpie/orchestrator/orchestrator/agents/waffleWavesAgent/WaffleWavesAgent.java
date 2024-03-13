package io.github.semanticpie.orchestrator.orchestrator.agents.waffleWavesAgent;

import io.github.semanticpie.orchestrator.orchestrator.Agent;
import io.github.semanticpie.orchestrator.orchestrator.exceptions.AgentException;
import io.github.semanticpie.orchestrator.services.JmanticService;
import lombok.extern.slf4j.Slf4j;
import org.ostis.scmemory.model.element.ScElement;
import org.ostis.scmemory.model.element.edge.EdgeType;
import org.ostis.scmemory.model.element.edge.ScEdge;
import org.ostis.scmemory.model.element.link.LinkType;
import org.ostis.scmemory.model.element.link.ScLinkString;
import org.ostis.scmemory.model.element.node.NodeType;
import org.ostis.scmemory.model.element.node.ScNode;
import org.ostis.scmemory.model.event.OnAddOutgoingEdgeEvent;
import org.ostis.scmemory.model.exception.ScMemoryException;
import org.ostis.scmemory.model.pattern.ScPattern;
import org.ostis.scmemory.model.pattern.pattern3.ScPattern3Impl;
import org.ostis.scmemory.websocketmemory.memory.element.ScEdgeImpl;
import org.ostis.scmemory.websocketmemory.memory.element.ScLinkStringImpl;
import org.ostis.scmemory.websocketmemory.memory.element.ScNodeImpl;
import org.ostis.scmemory.websocketmemory.memory.pattern.DefaultWebsocketScPattern;
import org.ostis.scmemory.websocketmemory.memory.pattern.SearchingPatternTriple;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.AliasPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.FixedPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.TypePatternElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class WaffleWavesAgent extends Agent {

    private final WaffleWavesService waffleWavesService;
    @Autowired
    public WaffleWavesAgent(JmanticService jmanticService, WaffleWavesService waffleWavesService) {
        this.waffleWavesService = waffleWavesService;
        this.jmanticService = jmanticService;
        this.context = jmanticService.getContext();
    }

    @Override
    public void subscribe() {
        try {
            context.resolveKeynode("action_waffle_waves", NodeType.CONST);
        }catch (ScMemoryException e){
            log.error(e.getLocalizedMessage());
        }
        this.subscribe("action_waffle_waves", (OnAddOutgoingEdgeEvent) this::onEventDo);
    }

    private void onEventDo(ScElement source, ScEdge edge, ScElement target) {
        try {
            log.info("WaffleWavesEvent");

            List<ScElement> apiList = new java.util.ArrayList<>(context.find(apiPattern(target)).findFirst().orElseThrow().filter(scElement -> !scElement.equals(target)).toList());

            log.info("api list: {}", apiList);
            ScNode playlistNode = null;
            ScNode userNode = null;
            int limit = 0;

            for(var element: apiList.stream().filter(scElement -> !(scElement instanceof ScEdgeImpl)).toList()){
                if(element instanceof ScLinkStringImpl){
                    limit = Integer.parseInt(context.getStringLinkContent((ScLinkString) element)
                            .replace("float:", "").replace("\"", ""));
                }
                else {
                    ScNode node = (ScNode) element;

                    if(((ScNode) element).getType() == NodeType.VAR_STRUCT){
                        userNode = node;
                    }else {
                        playlistNode = node;
                    }
                }
            }
            ScNode finalUserNode = userNode;
            ScNode finalPlaylistNode = playlistNode;
            apiList.add(target);
            Stream<ScElement> stream = apiList.stream().filter(scElement -> !scElement.equals(finalUserNode)).filter(scElement -> !scElement.equals(finalPlaylistNode));

            context.deleteElement(edge);

            if(playlistNode == null || userNode == null || limit ==0){
                log.warn("Incorrect api loaded!");
                return;
            }
            waffleWavesService.loadGenreWeights(userNode);
            List<ScElement> oldPlaylist = waffleWavesService.getAndDeleteOldPlaylist(playlistNode, userNode);
            log.warn("Old playlist: {}", oldPlaylist);
            List<ScElement> playlist =  waffleWavesService.createPlaylist(limit, oldPlaylist);
            log.warn("New playlist: {}", playlist.size());
            waffleWavesService.uploadPlaylist(playlistNode, playlist, userNode);
            waffleWavesService.getUserGenresMap().clear();
            context.deleteElements(stream);
        } catch (ScMemoryException e) {
            throw new AgentException(e);
        }
    }

    private ScPattern apiPattern(ScElement target){
        ScPattern pattern = new DefaultWebsocketScPattern();
        pattern.addElement(new SearchingPatternTriple(new FixedPatternElement(target),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_1")),
                new TypePatternElement<>(LinkType.LINK_VAR, new AliasPatternElement("playlist_limit")))
        );
        pattern.addElement(new SearchingPatternTriple(new FixedPatternElement(target),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_2")),
                new TypePatternElement<>(NodeType.VAR_TUPLE, new AliasPatternElement("playlist_node")))
        );

        pattern.addElement(new SearchingPatternTriple(new FixedPatternElement(target),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_3")),
                new TypePatternElement<>(NodeType.VAR_STRUCT, new AliasPatternElement("user_node")))
        );
        return pattern;
    }
}
