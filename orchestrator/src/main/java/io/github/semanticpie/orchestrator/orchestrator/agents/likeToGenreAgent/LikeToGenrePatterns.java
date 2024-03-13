package io.github.semanticpie.orchestrator.orchestrator.agents.likeToGenreAgent;

import org.ostis.scmemory.model.element.ScElement;
import org.ostis.scmemory.model.element.edge.EdgeType;
import org.ostis.scmemory.model.element.node.NodeType;
import org.ostis.scmemory.model.pattern.ScPattern;
import org.ostis.scmemory.websocketmemory.memory.pattern.DefaultWebsocketScPattern;
import org.ostis.scmemory.websocketmemory.memory.pattern.SearchingPatternTriple;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.AliasPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.FixedPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.TypePatternElement;
import org.springframework.stereotype.Component;

@Component
public class LikeToGenrePatterns {

    public ScPattern patternOfSearchingNodes(ScElement edge, NodeType first, NodeType second) {
        ScPattern pattern = new DefaultWebsocketScPattern();
        pattern.addElement(new SearchingPatternTriple(new TypePatternElement<>(first, new AliasPatternElement("node1")), new FixedPatternElement(edge), new TypePatternElement<>(second, new AliasPatternElement("node2"))));

        return pattern;
    }

    public ScPattern patternOfSearchingTrackGenre(ScElement track, ScElement nrelGenre){
        ScPattern pattern = new DefaultWebsocketScPattern();

        pattern.addElement(new SearchingPatternTriple(new FixedPatternElement(track), new TypePatternElement<>(EdgeType.D_COMMON_VAR, new AliasPatternElement("edge_1")), new TypePatternElement<>(NodeType.VAR_CLASS, new AliasPatternElement("genre"))));

        pattern.addElement(new SearchingPatternTriple(new FixedPatternElement(nrelGenre),new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_2")), new AliasPatternElement("edge_1")));

        return pattern;
    }
}
