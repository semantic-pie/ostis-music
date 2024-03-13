package io.github.semanticpie.derezhor.externalAgents.loafLoader.services.sync;

import io.github.semanticpie.derezhor.externalAgents.loafLoader.services.knowledger.KnowledgeService;
import io.github.semanticpie.derezhor.externalAgents.loafLoader.services.storager.StorageService;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.ostis.scmemory.model.exception.ScMemoryException;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@AllArgsConstructor
public class SyncResourcesServiceImpl implements SyncResourcesService {
    private final KnowledgeService knowledgeService;
    private final StorageService storageService;


    public static String getMimeType(InputStream inputStream) throws IOException {
        // Создаем парсер
        Parser parser = new AutoDetectParser();

        // Создаем обработчик содержимого
        BodyContentHandler handler = new BodyContentHandler();

        // Создаем метаданные
        Metadata metadata = new Metadata();

        // Контекст разбора
        ParseContext context = new ParseContext();

        try {
            // Разбираем содержимое и получаем метаданные
            parser.parse(inputStream, handler, metadata, context);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
        }

        // Получаем MIME-тип из метаданных
        MediaType mediaType = MediaType.parse(metadata.get(Metadata.CONTENT_TYPE));
        return mediaType.toString();
    }


    @Override
    public String sync(MultipartFile multipartFile) {
        String hash = null;
        try {

            log.info("START [upload][file][hash]");
            long hashTime = System.currentTimeMillis();
            InputStream inputStream = multipartFile.getInputStream();
            hash = DigestUtils.md5DigestAsHex(inputStream);
            inputStream.close();
            log.info("FINISH [upload][file][has] [{}]", System.currentTimeMillis() - hashTime);

            log.info("START [upload][file][minio]");
            long minioTime = System.currentTimeMillis();
            inputStream = multipartFile.getInputStream();
            syncWithStorage(hash, inputStream);
            inputStream.close();
            log.info("FINISH [upload][file][minio] [{}]", System.currentTimeMillis() - minioTime);

            log.info("FINISH [upload][file][ostis]");
            long scTime = System.currentTimeMillis();
            syncWithKnowladges(hash, multipartFile.getContentType());
            log.info("FINISH [upload][file][ostis] [{}]", System.currentTimeMillis() - scTime);

            return hash;
        } catch (ResourceAlreadyExistException e) {
            return hash;
        } catch (IOException | MinioException | ScMemoryException e) {
            throw new SyncException(e);
        }
    }

    @Override
    public void sync() {
        var files = storageService.getAllFiles();

        files.forEach(file -> {
            try {
                long scTime = System.currentTimeMillis();
                syncWithKnowladges(file.get().objectName(), getMimeType(storageService.getFile(file.get().objectName())));
                log.info("FINISH [upload][file][ostis] [{}]", System.currentTimeMillis() - scTime);
            } catch (NoSuchAlgorithmException | InvalidKeyException | ServerException | InsufficientDataException |
                     ErrorResponseException | IOException | InvalidResponseException | XmlParserException |
                     InternalException | ScMemoryException e) {
                throw new RuntimeException(e);
            }


        });


    }


    private void syncWithStorage(String hash, InputStream inputStream) throws IOException, MinioException {
        storageService.putFile(hash, inputStream);
    }

    private void syncWithKnowladges(String hash, String contentType) throws ScMemoryException {
        knowledgeService.putFile(hash, contentType);
    }

    @Override
    public InputStream resourceInputStream(String hash) {
        try {
            return storageService.getFile(hash);
        } catch (RuntimeException e) {
            throw new SyncException("Can't load this resource", e);
        }
    }
}
