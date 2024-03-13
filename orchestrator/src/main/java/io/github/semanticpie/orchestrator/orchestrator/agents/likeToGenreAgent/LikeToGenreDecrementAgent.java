package io.github.semanticpie.orchestrator.orchestrator.agents.likeToGenreAgent;

import io.github.semanticpie.orchestrator.orchestrator.Agent;
import io.github.semanticpie.orchestrator.services.JmanticService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ostis.scmemory.model.element.ScElement;
import org.ostis.scmemory.model.element.edge.ScEdge;
import org.ostis.scmemory.model.event.OnAddOutgoingEdgeEvent;
import org.ostis.scmemory.model.exception.ScMemoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class LikeToGenreDecrementAgent extends Agent {

    private final LikeToGenreService service;

    @Autowired
    public LikeToGenreDecrementAgent(JmanticService jmanticService, LikeToGenreService service) {
        this.service = service;
        this.jmanticService = jmanticService;
        this.context = jmanticService.getContext();
    }

    @Override
    public void subscribe() {
        this.subscribe("unlike", (OnAddOutgoingEdgeEvent) this::onEventDo);
    }



    private void onEventDo(ScElement source, ScEdge edge, ScElement target) {
        try {
            service.incrementator(target, -1);
            context.deleteElement(target);
        } catch (ScMemoryException e) {
            throw new RuntimeException(e);
        }
    }
}
