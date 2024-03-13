package io.github.semanticpie.derezhor.externalAgents.loafLoader.services.sync;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface SyncResourcesService {
    String sync(MultipartFile multipartFile);

    void sync();
    InputStream resourceInputStream(String hash);
}
