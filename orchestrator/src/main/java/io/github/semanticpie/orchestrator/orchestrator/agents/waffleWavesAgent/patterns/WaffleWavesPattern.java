package io.github.semanticpie.orchestrator.orchestrator.agents.waffleWavesAgent.patterns;

import org.ostis.scmemory.model.element.ScElement;
import org.ostis.scmemory.model.element.edge.EdgeType;
import org.ostis.scmemory.model.element.link.LinkType;
import org.ostis.scmemory.model.element.node.NodeType;
import org.ostis.scmemory.model.pattern.ScPattern;
import org.ostis.scmemory.websocketmemory.memory.pattern.DefaultWebsocketScPattern;
import org.ostis.scmemory.websocketmemory.memory.pattern.GeneratingPatternTriple;
import org.ostis.scmemory.websocketmemory.memory.pattern.SearchingPatternTriple;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.AliasPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.FixedPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.TypePatternElement;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WaffleWavesPattern {
    private int structEdgeIndex = 0;
    public ScPattern scPattern5ForPlaylist(ScElement userNode, ScElement relation) {
        ScPattern pattern = new DefaultWebsocketScPattern();
        pattern.addElement(new SearchingPatternTriple(new FixedPatternElement(relation),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_1")),
                new TypePatternElement<>(NodeType.VAR, new AliasPatternElement("track"))));

        pattern.addElement(new SearchingPatternTriple(new FixedPatternElement(userNode),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_2")),
                new AliasPatternElement("edge_1")));

        return pattern;
    }

    public ScPattern findPlaylistTrackNodestPattern(ScElement userNode, ScElement playlistNode) {
        ScPattern pattern = new DefaultWebsocketScPattern();

        pattern.addElement(new SearchingPatternTriple(new FixedPatternElement(playlistNode),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("_edge2")),
                new TypePatternElement<>(NodeType.VAR, new AliasPatternElement("_track"))));

        pattern.addElement(new SearchingPatternTriple(new FixedPatternElement(userNode),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("_edge3")),
                new AliasPatternElement("_track")));

        pattern.addElement(new SearchingPatternTriple(new FixedPatternElement(userNode),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("_edge4")),
                new AliasPatternElement("_edge2")));

        return pattern;
    }

    public ScPattern linkToStructurePattern(ScElement structure, List<? extends ScElement> elements) {
        ScPattern pattern = new DefaultWebsocketScPattern();

        for (var element : elements) {
            pattern.addElement(new GeneratingPatternTriple(new FixedPatternElement(structure),
                    new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_" + structEdgeIndex)),
                    new FixedPatternElement(element)));
            structEdgeIndex++;
        }
        return pattern;
    }

    public ScPattern trackPattern(ScElement genreNode, ScElement conceptTrack, ScElement nrelGenre) {
        ScPattern pattern = new DefaultWebsocketScPattern();

        pattern.addElement(new SearchingPatternTriple(
                new TypePatternElement<>(NodeType.VAR, new AliasPatternElement("track")),
                new TypePatternElement<>(EdgeType.D_COMMON_VAR, new AliasPatternElement("edge_1")),
                new FixedPatternElement(genreNode)
        ));

        pattern.addElement(new SearchingPatternTriple(
                new FixedPatternElement(conceptTrack),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_2")),
                new AliasPatternElement("track")
        ));

        pattern.addElement(new SearchingPatternTriple(new FixedPatternElement(nrelGenre),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_3")),
                new AliasPatternElement("edge_1")));
        return pattern;
    }

    public ScPattern userGenresPattern(ScElement userNode, ScElement userGenresTuple, ScElement nrelWeight) {

        ScPattern pattern = new DefaultWebsocketScPattern();

        pattern.addElement(new SearchingPatternTriple(
                new FixedPatternElement(userGenresTuple),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_1")),
                new TypePatternElement<>(NodeType.VAR_CLASS, new AliasPatternElement("genre"))));

        pattern.addElement(new SearchingPatternTriple(
                new AliasPatternElement("genre"),
                new TypePatternElement<>(EdgeType.D_COMMON_VAR, new AliasPatternElement("edge_2")),
                new TypePatternElement<>(LinkType.LINK_VAR, new AliasPatternElement("weight"))));

        pattern.addElement(new SearchingPatternTriple(
                new FixedPatternElement(nrelWeight),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_3")),
                new AliasPatternElement("edge_2")));


        //Add to struct
        pattern.addElement(new SearchingPatternTriple(
                new FixedPatternElement(userNode),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_4")),
                new AliasPatternElement("genre")
        ));

        pattern.addElement(new SearchingPatternTriple(
                new FixedPatternElement(userNode),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_6")),
                new AliasPatternElement("edge_1")
        ));

        pattern.addElement(new SearchingPatternTriple(
                new FixedPatternElement(userNode),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_7")),
                new AliasPatternElement("edge_2")
        ));

        pattern.addElement(new SearchingPatternTriple(
                new FixedPatternElement(userNode),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_8")),
                new AliasPatternElement("edge_3")
        ));

        return pattern;
    }
}
