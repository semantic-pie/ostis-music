package io.github.semanticpie.orchestrator.orchestrator.agents.trackAgent;

import com.mpatric.mp3agic.*;
import io.github.semanticpie.orchestrator.orchestrator.agents.trackAgent.models.TrackData;
import io.github.semanticpie.orchestrator.orchestrator.Agent;
import io.github.semanticpie.orchestrator.orchestrator.exceptions.AgentException;
import io.github.semanticpie.orchestrator.orchestrator.agents.trackAgent.services.impl.TrackServiceException;
import io.github.semanticpie.orchestrator.orchestrator.agents.trackAgent.services.TrackService;
import io.github.semanticpie.orchestrator.services.JmanticService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.ostis.scmemory.model.element.ScElement;
import org.ostis.scmemory.model.element.edge.ScEdge;
import org.ostis.scmemory.model.event.OnAddIngoingEdgeEvent;
import org.ostis.scmemory.model.exception.ScMemoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
public class TrackAgent extends Agent {

    private final MinioClient minioClient;
    private final TrackService trackService;

    @Value("${application.loaf-loader-url}")
    private String loafLoaderUrl;

    @Value("${minio.bucket}")
    public String BUCKET_NAME;

    @Autowired
    public TrackAgent(JmanticService jmanticService, MinioClient minioClient, TrackService trackService) {
        this.jmanticService = jmanticService;
        this.context = jmanticService.getContext();
        this.minioClient = minioClient;
        this.trackService = trackService;
    }

    @Override
    public void subscribe() {
        this.subscribe("format_audio_mpeg", (OnAddIngoingEdgeEvent) this::onEventDo);
        this.subscribe("format_audio_flac", (OnAddIngoingEdgeEvent) this::onEventDo);
    }

    private void onEventDo(ScElement source, ScEdge edge, ScElement target) {
        try {
            log.info("EVENT EVENT EVENT");

            String hash = jmanticService.getSysIdtf(target);

            TrackData trackData = getTrackMetadataByHash(hash);
            trackService.uploadTrackToScMachine(trackData);
            log.info("uploaded [{}]", hash);
        } catch (ScMemoryException e) {
            throw new AgentException(e);
        }
    }

    public TrackData getTrackMetadataByHash(String hash) {
        try {
            File resource = loadFileFromLoafLoader(hash);
            try {
                resource = changeExtension(resource, ".mp3");
                return getTrackData(resource, hash);
            } catch (IOException | InvalidDataException | RuntimeException e) {
                resource = changeExtension(resource, ".flac");
                return getVerboseCommentsTrackData(resource, hash);
            } catch (UnsupportedTagException e) {
                throw new TrackServiceException("Can't get track metadata by UUID [" + hash + "]");
            }
        } catch (IOException | RuntimeException e) {
            log.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

    }

    private File loadFileFromLoafLoader(String hash) throws IOException {
        File resource = File.createTempFile(hash, ".tmp");
        try (InputStream in = minioClient.getObject(GetObjectArgs.builder().bucket(BUCKET_NAME).object(hash).build())) {
            java.nio.file.Files.copy(
                    in,
                    resource.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return resource;
    }

    private TrackData getVerboseCommentsTrackData(File resource, String hash) {

        AudioFile audioFile = null;
        try {
            audioFile = AudioFileIO.read(resource);
            Tag tag = audioFile.getTag();

            AudioHeader header = audioFile.getAudioHeader();

            String artist = tag.getFirst(FieldKey.ARTIST);
            String album = tag.getFirst(FieldKey.ALBUM);
            String title = tag.getFirst(FieldKey.TITLE);
            String genre = tag.getFirst(FieldKey.GENRE);
            String year = tag.getFirst(FieldKey.YEAR);
            String tarckNr = tag.getFirst(FieldKey.TRACK);
            Integer bitRate = getBitRate(header.getBitRate());
            Long length = (long) header.getTrackLength();
            return TrackData.builder()
                    .hash(hash)
                    .title(title)
                    .album(album)
                    .artist(artist)
                    .genre(genre)
                    .releaseYear(year)
                    .trackNumber(tarckNr)
                    .bitrate(bitRate)
                    .lengthInMilliseconds(length)
                    .build();
        } catch (CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Integer getBitRate(String bitRate) {
        try {
            return Integer.parseInt(bitRate);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private TrackData getTrackData(File resource, String hash) throws IOException, InvalidDataException, UnsupportedTagException {
        Mp3File mp3file = new Mp3File(resource);

        ID3v2 id3v2 = mp3file.getId3v2Tag();
        return TrackData.builder()
                .hash(hash)
                .title(id3v2.getTitle())
                .album(id3v2.getAlbum())
                .artist(id3v2.getArtist())
                .genre(id3v2.getGenreDescription())
                .releaseYear(id3v2.getYear())
                .trackNumber(id3v2.getTrack())
                .bitrate(mp3file.getBitrate())
                .lengthInMilliseconds(mp3file.getLengthInMilliseconds())
                .build();
    }
    public static File changeExtension(File f, String newExtension) {
        var path = Path.of(f.getPath());
        try {
            return  Files.move(path, path.resolveSibling(path.getFileName() + newExtension)).toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
