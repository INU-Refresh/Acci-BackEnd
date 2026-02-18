package refresh.acci.domain.analysis.application.port.out;

import java.nio.file.Path;
import java.time.Duration;

public interface VideoStoragePort {
    void uploadFile(String key, Path filePath);
    String generatePresignedUrl(String key, Duration ttl);
}
