package io.github.semanticpie.orchestrator.config;

import io.github.semanticpie.orchestrator.orchestrator.Agent;
import io.github.semanticpie.orchestrator.orchestrator.Orchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;

@Slf4j
@Configuration
public class OrchestratorConfiguration {

    private final Orchestrator orchestrator;
    private final List<Agent> agents;

    @Autowired
    public OrchestratorConfiguration(List<Agent> agents, Orchestrator orchestrator) {
        this.agents = agents;
        this.orchestrator = orchestrator;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        agents.forEach(orchestrator::addAgent);
        log.info("Agents: {}", agents);
        orchestrator.listen();
    }
}
