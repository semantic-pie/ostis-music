package io.github.semanticpie.orchestrator.services;

import lombok.Getter;
import org.ostis.api.context.DefaultScContext;
import org.ostis.scmemory.model.element.ScElement;
import org.ostis.scmemory.model.element.edge.EdgeType;
import org.ostis.scmemory.model.element.link.LinkType;
import org.ostis.scmemory.model.element.link.ScLinkString;
import org.ostis.scmemory.model.element.node.NodeType;
import org.ostis.scmemory.model.exception.ScMemoryException;
import org.ostis.scmemory.model.pattern.pattern5.ScPattern5Impl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter
@Service
public class JmanticService {
    private final DefaultScContext context;

    @Autowired
    public JmanticService(DefaultScContext context) {
        this.context = context;
    }

    public String getSysIdtf(ScElement element) throws ScMemoryException {
        var mainIdtf = context.resolveKeynode("nrel_system_identifier", NodeType.CONST);

        var findedPentad = context.find(new ScPattern5Impl<>(
                element, EdgeType.D_COMMON_VAR, LinkType.LINK_VAR, EdgeType.ACCESS_VAR_POS_PERM, mainIdtf
        )).findFirst().orElseThrow(ScMemoryException::new);

        return context.getStringLinkContent((ScLinkString) findedPentad.get3());
    }

}

