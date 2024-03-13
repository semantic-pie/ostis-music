package io.github.semanticpie.derezhor.common.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ostis.api.context.DefaultScContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeepAliveService {
    private final DefaultScContext context;

    @Scheduled(fixedDelayString = "${connection-timeout:2000}")
    private void onSchedule() {
       checkConnection();
    }

    /**
     * If the connection is lost, it tries to reconnect.
     */
    public void checkConnection() {
        try {
            boolean isOpen = false;

            try {
                isOpen = context.memory().isOpen();
            } catch (NullPointerException ignored) {}

            if (!isOpen) {
                context.memory().open();
                log.info("Connected to sc-machine");
            }

        } catch (Exception ignored) {
            log.error("Connection lost. Try reconnect...");
        }
    }
}
