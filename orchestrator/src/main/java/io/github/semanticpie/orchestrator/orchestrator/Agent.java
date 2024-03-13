package io.github.semanticpie.orchestrator.orchestrator;

import io.github.semanticpie.orchestrator.orchestrator.exceptions.AgentException;
import io.github.semanticpie.orchestrator.services.JmanticService;
import lombok.extern.slf4j.Slf4j;
import org.ostis.api.context.DefaultScContext;
import org.ostis.scmemory.model.event.OnEdgeEvent;
import org.ostis.scmemory.model.exception.ScMemoryException;

@Slf4j
public abstract class Agent {

    protected DefaultScContext context;

    protected JmanticService jmanticService;
    private Long eventId;

    public void subscribe() {}

    protected void subscribe(String subscribedNode, OnEdgeEvent edgeEvent) {
        try {
            context.findKeynode(subscribedNode).ifPresent(subscriber -> {
                try {
                    context.subscribeOnEvent(subscriber, edgeEvent);
                } catch (ScMemoryException e) {
                    throw new AgentException("Error while subscribing " +  this.getClass().getName() + " " + edgeEvent.getEventType().name(), e);
                }
            });
            log.info("Agent {} subscribed. Event: {}", this.getClass().getSimpleName(), edgeEvent.getEventType().name());
        } catch (AgentException | ScMemoryException  e) {
            throw new AgentException(e);
        }
    }

    public void unsubscribe() {
        try {
            context.unSubscribeOnEvent(eventId);
        } catch (ScMemoryException ignored) {}
    }
}
