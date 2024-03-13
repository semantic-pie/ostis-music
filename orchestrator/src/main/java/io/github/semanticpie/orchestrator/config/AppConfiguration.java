package io.github.semanticpie.orchestrator.config;

import lombok.extern.slf4j.Slf4j;
import org.ostis.api.context.DefaultScContext;
import org.ostis.scmemory.websocketmemory.memory.SyncOstisScMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Configuration
@ComponentScan("io.github.semanticpie.orchestrator")
public class AppConfiguration {

    @Value("${application.sc-machine.url}")
    private String scMachineURL;

    @Bean
    public DefaultScContext contextBean() {
        try {
            var memory = new SyncOstisScMemory(new URI(scMachineURL));
            memory.open();
            return new DefaultScContext(memory);
        } catch (URISyntaxException e) {
            log.error("SC-Memory URI is invalid: {}", scMachineURL);
            throw new RuntimeException(e);
        } catch (Exception  e) {
            log.error("Connection error. Can't open sc-memory {}.", scMachineURL);
            throw new RuntimeException(e);
        }
    }
}
