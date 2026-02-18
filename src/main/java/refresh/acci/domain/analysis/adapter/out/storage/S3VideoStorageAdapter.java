package refresh.acci.domain.analysis.adapter.out.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import refresh.acci.domain.analysis.application.port.out.VideoStoragePort;
import refresh.acci.global.util.S3FileService;

import java.nio.file.Path;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class S3VideoStorageAdapter implements VideoStoragePort {

    private final S3FileService s3FileService;

    @Override
    public void uploadFile(String key, Path filePath) {
        s3FileService.uploadFile(key, filePath);
    }

    @Override
    public String generatePresignedUrl(String key, Duration ttl) {
        return s3FileService.generatePresignedUrl(key, ttl);
    }
}
