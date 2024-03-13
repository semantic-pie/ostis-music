package io.github.semanticpie.derezhor.externalAgents.loafLoader.api;

import io.github.semanticpie.derezhor.externalAgents.loafLoader.services.sync.SyncResourcesService;
import io.github.semanticpie.derezhor.common.errorsResponse.ApiPieTunesErrorInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/v1/loafloader")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.HEAD, RequestMethod.GET, RequestMethod.POST})
public class UploadController {

    private final SyncResourcesService syncResourcesService;

    @Autowired
    public UploadController(SyncResourcesService syncResourcesService) {
        this.syncResourcesService = syncResourcesService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getResource(@PathVariable String id, HttpServletRequest request) {
        try {
            var resource = syncResourcesService.resourceInputStream(id);

            HttpHeaders headers = new HttpHeaders();

            // Проверяем User-Agent на наличие Safari
            String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
            if (userAgent != null && userAgent.contains("Safari")) {
                headers.setContentType(MediaType.valueOf("audio/mpeg"));
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(resource));
        } catch (RuntimeException e) {
            log.warn("OMG!!! Why so bad, bro. Error: {}", e.getMessage());
//            log.error("errr", e);
            return new ResponseEntity<>(
                    new ApiPieTunesErrorInfo(HttpStatus.CONFLICT.value(), request.getRequestURL(), e.getMessage()),
                    HttpStatus.CONFLICT);
            // Egor: я бы лучше бросал какой нибудь exception, а GlobalHttpHandler его уже ловил
        }
    }

    @PostMapping()
    public ResponseEntity<?> postMultipleResource(@RequestParam("resources") List<MultipartFile> files, HttpServletRequest request) {
        try {
            log.info("START [upload]");
            long time = System.currentTimeMillis();
            StringBuilder result = new StringBuilder();
            for (var file : files) {
                long fileTime = System.currentTimeMillis();
                log.info("START [upload][file]");
                var id = syncResourcesService.sync(file);
                result.append(id).append('\n');
                log.info("FINISH [upload][file] [{}]", System.currentTimeMillis() - fileTime);
            }
            log.info("FINISH [upload] [{}]", System.currentTimeMillis() - time);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.warn("OMG!!! Why so bad, bro. Error: {}", e.getMessage());
            log.error("errr", e);
            return new ResponseEntity<>(
                    new ApiPieTunesErrorInfo(HttpStatus.CONFLICT.value(), request.getRequestURL(), e.getMessage()),
                    HttpStatus.CONFLICT);
            // Egor: я бы лучше бросал какой нибудь exception, а GlobalHttpHandler его уже ловил
        }
    }


    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
      syncResourcesService.sync();
    }
}
