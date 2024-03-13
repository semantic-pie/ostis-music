package io.github.semanticpie.orchestrator.config;

import io.github.semanticpie.orchestrator.orchestrator.Agent;
import lombok.extern.slf4j.Slf4j;
import org.ostis.api.context.DefaultScContext;

import java.util.List;
import java.util.TimerTask;

@Slf4j
public class ReopenTask extends TimerTask {
    private final DefaultScContext context;
    private final List<Agent> agents;

    public ReopenTask(DefaultScContext context, List<Agent> agents) {
        this.context = context;
        this.agents = agents;
    }


    @Override
    public void run() {
        try {
            boolean isOpen = false;

            try {
                isOpen = context.memory().isOpen();
            } catch (NullPointerException ignored) {}

            if (!isOpen) {
                context.memory().open();
                agents.forEach(Agent::unsubscribe);
                agents.forEach(Agent::subscribe);
            }

        } catch (Exception ignored) {}
    }
}
