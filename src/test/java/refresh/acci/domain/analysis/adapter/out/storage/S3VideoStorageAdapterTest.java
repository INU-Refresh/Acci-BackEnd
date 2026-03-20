package refresh.acci.domain.analysis.adapter.out.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import refresh.acci.global.util.S3FileService;

import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class S3VideoStorageAdapterTest {

    private S3FileService s3FileService;
    private S3VideoStorageAdapter s3VideoStorageAdapter;

    @BeforeEach
    void setUp() {
        s3FileService = mock(S3FileService.class);
        s3VideoStorageAdapter = new S3VideoStorageAdapter(s3FileService);
    }

    @Test
    @DisplayName("파일 업로드를 S3FileService에 위임한다.")
    void uploadFile() {
        // given
        String key = "videos/test.mp4";
        Path filePath = Path.of("/tmp/test.mp4");

        // when
        s3VideoStorageAdapter.uploadFile(key, filePath);

        // then
        verify(s3FileService).uploadFile(key, filePath);
    }

    @Test
    @DisplayName("Presigned URL 생성을 S3FileService에 위임하고 결과를 반환한다.")
    void generatePresignedUrl() {
        // given
        String key = "videos/test.mp4";
        Duration ttl = Duration.ofMinutes(10);
        String expectedUrl = "https://signed-url";

        when(s3FileService.generatePresignedUrl(key, ttl)).thenReturn(expectedUrl);

        // when
        String result = s3VideoStorageAdapter.generatePresignedUrl(key, ttl);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        verify(s3FileService).generatePresignedUrl(key, ttl);
    }
}
