package io.github.semanticpie.orchestrator.orchestrator;

import io.github.semanticpie.orchestrator.config.ReopenTask;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.ostis.api.context.DefaultScContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

@Slf4j
@Service
public class Orchestrator {


    @Value("${connection-timeout:2000}")
    private Integer pingTime;

    private final List<Agent> agents;
    private final DefaultScContext context;
    private final Timer timer;

    @Autowired
    public Orchestrator(DefaultScContext context) {
        this.context = context;
        this.agents = new ArrayList<>();
        this.timer = new Timer();
    }

    public void addAgent(Agent agent) {
        agents.add(agent);
    }

    public void removeAgent(Agent agent) {
        agents.remove(agent);
    }

    public void listen() {
        agents.forEach(Agent::subscribe);
        this.timer.scheduleAtFixedRate(new ReopenTask(context, agents), pingTime, pingTime);
    }

    @PreDestroy
    private void destroy() {
        agents.forEach(Agent::unsubscribe);
        try {
            context.memory().close();
        } catch (Exception ignored) {}
    }
}
