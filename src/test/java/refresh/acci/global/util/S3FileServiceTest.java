package refresh.acci.global.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class S3FileServiceTest {

    private S3Client s3Client;
    private S3Presigner s3Presigner;
    private S3FileService s3FileService;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        s3Presigner = mock(S3Presigner.class);
        s3FileService = new S3FileService(s3Client, s3Presigner);

        ReflectionTestUtils.setField(s3FileService, "bucket", "test-bucket");
    }

    @Test
    @DisplayName("파일 경로 업로드 시, S3Client에 bucket과 key를 전달한다.")
    void uploadFile() {
        // given
        String key = "test-key";
        Path path = Path.of("test-file-path");

        // when
        s3FileService.uploadFile(key, path);

        // then
        verify(s3Client).putObject(
                argThat((PutObjectRequest req) ->
                        req.bucket().equals("test-bucket") && req.key().equals(key)
                        ),
                eq(path)
        );
    }

    @Test
    @DisplayName("멀티파트 파일 업로드에 성공하면 생성된 S3 key를 반환한다.")
    void uploadMultipartFile() {
        // given
        MultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "dummy-content".getBytes()
        );

        // when
        String result = s3FileService.uploadMultipartFile("analysis", file);

        // then
        assertThat(result).startsWith("analysis/");
        assertThat(result).endsWith(".mp4");

        verify(s3Client).putObject(
                argThat((PutObjectRequest req) ->
                        req.bucket().equals("test-bucket") &&
                                req.key().startsWith("analysis/") &&
                                req.key().endsWith(".mp4") &&
                                req.contentType().equals("video/mp4") &&
                                req.contentLength().equals(file.getSize())
                ),
                any(RequestBody.class)
        );
    }

    @Test
    @DisplayName("파일명에 확장자가 없으면 jpg 확장자를 사용한다.")
    void uploadMultipartFile_useDefaultJpgExtension() {
        // given
        MultipartFile file = new MockMultipartFile(
                "file",
                "video",
                "image/jpeg",
                "dummy-content".getBytes()
        );

        // when
        String result = s3FileService.uploadMultipartFile("analysis", file);

        // then
        assertThat(result).startsWith("analysis/");
        assertThat(result).endsWith(".jpg");
    }

    @Test
    @DisplayName("멀티파트 업로드 중 IOException이 발생하면 S3_UPLOAD_FAILED 예외를 던진다.")
    void uploadMultipartFile_fail_whenIOExceptionOccurs() {
        // given
        MultipartFile file = spy(new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "dummy-content".getBytes()
        ));

        try {
            doThrow(new IOException("io error")).when(file).getInputStream();
        } catch (IOException e) {
            fail("테스트 설정 실패");
        }

        // when / then
        assertThatThrownBy(() -> s3FileService.uploadMultipartFile("analysis", file))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.S3_UPLOAD_FAILED);
    }

    @Test
    @DisplayName("멀티파트 업로드 중 SdkException이 발생하면 S3_UPLOAD_FAILED 예외를 던진다.")
    void uploadMultipartFile_fail_whenSdkExceptionOccurs() {
        // given
        MultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "dummy-content".getBytes()
        );

        doThrow(SdkClientException.create("s3 error"))
                .when(s3Client)
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // when / then
        assertThatThrownBy(() -> s3FileService.uploadMultipartFile("analysis", file))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.S3_UPLOAD_FAILED);
    }

    @Test
    @DisplayName("Presigned URL 생성 시 presigner가 반환한 URL 문자열을 반환한다")
    void generatePresignedUrl() throws Exception {
        // given
        String key = "videos/test.mp4";
        Duration duration = Duration.ofMinutes(10);
        String expectedUrl = "https://example.com/presigned-url";

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url()).thenReturn(new URL(expectedUrl));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        // when
        String result = s3FileService.generatePresignedUrl(key, duration);

        // then
        assertThat(result).isEqualTo(expectedUrl);

        verify(s3Presigner).presignGetObject(
                argThat((GetObjectPresignRequest req) -> {
                    GetObjectRequest getObjectRequest = req.getObjectRequest();
                    return req.signatureDuration().equals(duration)
                            && getObjectRequest.bucket().equals("test-bucket")
                            && getObjectRequest.key().equals(key);
                })
        );
    }
}
