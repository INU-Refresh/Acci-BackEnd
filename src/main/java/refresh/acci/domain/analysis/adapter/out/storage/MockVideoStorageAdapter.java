package refresh.acci.domain.analysis.adapter.out.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import refresh.acci.domain.analysis.application.port.out.VideoStoragePort;

import java.nio.file.Path;
import java.time.Duration;

@Profile("local")
@Component
public class MockVideoStorageAdapter implements VideoStoragePort {

    @Override
    public void uploadFile(String key, Path filePath) {
        return;
    }

    @Override
    public String generatePresignedUrl(String key, Duration ttl) {
        return "http://localhost/mock/video.mp4";
    }
}
