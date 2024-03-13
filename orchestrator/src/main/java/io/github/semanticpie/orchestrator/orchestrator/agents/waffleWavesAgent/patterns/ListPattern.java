package io.github.semanticpie.orchestrator.orchestrator.agents.waffleWavesAgent.patterns;

import lombok.Getter;
import org.ostis.scmemory.model.element.ScElement;
import org.ostis.scmemory.model.element.edge.EdgeType;
import org.ostis.scmemory.model.pattern.ScPattern;
import org.ostis.scmemory.websocketmemory.memory.pattern.DefaultWebsocketScPattern;
import org.ostis.scmemory.websocketmemory.memory.pattern.GeneratingPatternTriple;
import org.ostis.scmemory.websocketmemory.memory.pattern.SearchingPatternTriple;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.AliasPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.FixedPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.TypePatternElement;

import java.util.List;

public class ListPattern {

    @Getter
    private final ScPattern pattern;
    private int listEdgeIndex = 0;

    public ListPattern(List<ScElement> list, ScElement start, ScElement next, ScElement end){
         this.pattern = new DefaultWebsocketScPattern();

        for (int i = 0; i < list.size() - 1; i++) {
            addListSection(list.get(i), list.get(i + 1), next);
        }

        pattern.addElement(new SearchingPatternTriple(
                new FixedPatternElement(start),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_" + listEdgeIndex)),
                new FixedPatternElement(list.get(0))));
        listEdgeIndex++;
        pattern.addElement(new SearchingPatternTriple(
                new FixedPatternElement(end),
                new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM,
                        new AliasPatternElement("edge_" + listEdgeIndex)),
                new FixedPatternElement(list.get(list.size() - 1))));
        listEdgeIndex++;
    }

    private void addListSection(ScElement source, ScElement target, ScElement relation) {
        int edge1 = listEdgeIndex;
        pattern.addElement(
                new GeneratingPatternTriple(
                        new FixedPatternElement(source),
                        new TypePatternElement<>(EdgeType.D_COMMON_VAR, new AliasPatternElement("edge_" + listEdgeIndex)),
                        new FixedPatternElement(target)));
        listEdgeIndex++;
        pattern.addElement(
                new GeneratingPatternTriple(
                        new FixedPatternElement(relation),
                        new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_" + listEdgeIndex)),
                        new AliasPatternElement("edge_" + edge1)));
        listEdgeIndex++;
    }
}
