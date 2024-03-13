package io.github.semanticpie.derezhor.externalAgents.loafLoader.services.knowledger;

import org.ostis.scmemory.model.exception.ScMemoryException;
import org.springframework.web.multipart.MultipartFile;

public interface KnowledgeService {
    void putFile(String resourceIdtf, String contentType) throws ScMemoryException;
}
