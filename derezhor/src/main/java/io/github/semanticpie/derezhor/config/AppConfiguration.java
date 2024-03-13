package io.github.semanticpie.derezhor.config;

import lombok.extern.slf4j.Slf4j;
import org.ostis.api.context.DefaultScContext;
import org.ostis.scmemory.model.ScMemory;
import org.ostis.scmemory.websocketmemory.memory.SyncOstisScMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.URI;
import java.net.URISyntaxException;


@Slf4j
@Configuration
@ComponentScan("io.github.semanticpie.derezhor")
@EnableScheduling
public class AppConfiguration {

    @Value("${derezhor.sc-machine.url}")
    private String scMachineURL;

    @Bean
    protected DefaultScContext contextBean() throws URISyntaxException {
        // Now scheduler opens the memory
        return new DefaultScContext(new SyncOstisScMemory(new URI(scMachineURL)));
    }
}
