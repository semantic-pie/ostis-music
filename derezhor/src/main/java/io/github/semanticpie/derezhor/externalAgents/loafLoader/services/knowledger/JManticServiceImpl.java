package io.github.semanticpie.derezhor.externalAgents.loafLoader.services.knowledger;

import io.github.semanticpie.derezhor.common.services.CacheService;
import io.github.semanticpie.derezhor.externalAgents.loafLoader.services.sync.ResourceAlreadyExistException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ostis.api.context.DefaultScContext;
import org.ostis.scmemory.model.element.edge.EdgeType;
import org.ostis.scmemory.model.element.node.NodeType;
import org.ostis.scmemory.model.exception.ScMemoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@AllArgsConstructor
public class JManticServiceImpl implements KnowledgeService {

    private final DefaultScContext context;
    private final CacheService cacheService;

    @Override
    public void putFile(String hash, String contentType) throws ScMemoryException {
        try {
            createResource(hash, contentType);
        } catch (ScMemoryException e) {
            throw new ScMemoryException(e);
        }

    }

    private void createResource(String resourceIdtf, String contentType) throws ScMemoryException {
        if (context.findKeynode(resourceIdtf).isPresent()) {
            log.warn("RESOURCE {} EXIST", resourceIdtf);
            return;
        }

        log.info("createResource {}", resourceIdtf);

        var resource = context.resolveKeynode(resourceIdtf, NodeType.CONST);

        var noRolFormat = cacheService.get("nrel_format");
        var classFormat = cacheService.get("format");
        var resourceFormat = cacheService.get(toContentType(contentType), NodeType.CONST_CLASS);

        context.createEdge(EdgeType.ACCESS_CONST_POS_PERM, classFormat, resourceFormat);
        var edge = context.createEdge(EdgeType.D_COMMON_CONST, resource, resourceFormat);
        context.createEdge(EdgeType.ACCESS_CONST_POS_PERM, noRolFormat, edge);
    }

    private String toContentType(String contentType) {
        if (contentType != null && !contentType.isEmpty())
            return "format_" + contentType.replace('/', '_');
        else
            return "undefined_format";
    }
}
