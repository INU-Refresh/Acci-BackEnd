package refresh.acci.domain.analysis.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import refresh.acci.domain.analysis.application.port.out.AnalysisRepositoryPort;
import refresh.acci.domain.analysis.application.port.out.VideoStoragePort;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetVideoUrlUseCaseTest {

    private AnalysisRepositoryPort analysisRepository;
    private VideoStoragePort videoStorage;
    private GetVideoUrlUseCase getVideoUrlUseCase;

    @BeforeEach
    void setUp() {
        analysisRepository = mock(AnalysisRepositoryPort.class);
        videoStorage = mock(VideoStoragePort.class);
        getVideoUrlUseCase = new GetVideoUrlUseCase(analysisRepository, videoStorage);
    }

    @Test
    @DisplayName("본인 분석 영상이면 presigned url을 반환한다.")
    void getVideoUrl() {
        // given
        UUID analysisId = UUID.randomUUID();
        Long userId = 1L;
        String videoS3Key = "videos/test.mp4";
        String expectedUrl = "https://signed-url";

        Analysis analysis = mock(Analysis.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(analysisRepository.getById(analysisId)).thenReturn(analysis);
        when(analysis.getUserId()).thenReturn(userId);
        when(userDetails.getId()).thenReturn(userId);
        when(analysis.getVideoS3Key()).thenReturn(videoS3Key);
        when(videoStorage.generatePresignedUrl(videoS3Key, Duration.ofMinutes(10))).thenReturn(expectedUrl);

        // when
        String result = getVideoUrlUseCase.getVideoUrl(analysisId, userDetails);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        verify(analysisRepository).getById(analysisId);
        verify(videoStorage).generatePresignedUrl(videoS3Key, Duration.ofMinutes(10));
    }

    @Test
    @DisplayName("유저 정보가 없으면 접근 거부 예외가 발생한다.")
    void getVideoUrl_fail_whenUserDetailsIsNull() {
        // given
        UUID analysisId = UUID.randomUUID();
        Analysis analysis = mock(Analysis.class);

        when(analysisRepository.getById(analysisId)).thenReturn(analysis);

        // when / then
        assertThatThrownBy(() -> getVideoUrlUseCase.getVideoUrl(analysisId, null))
                .isInstanceOf(CustomException .class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED_TO_ANALYSIS);

        verify(analysisRepository).getById(analysisId);
        verify(videoStorage, never()).generatePresignedUrl(anyString(), any());
    }

    @Test
    @DisplayName("분석 소유자와 요청 사용자가 다르면 접근 거부 예외가 발생한다.")
    void getVideoUrl_fail_whenUserIsNotOwner() {
        // given
        UUID analysisId = UUID.randomUUID();

        Analysis analysis = mock(Analysis.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(analysisRepository.getById(analysisId)).thenReturn(analysis);
        when(analysis.getUserId()).thenReturn(1L);
        when(userDetails.getId()).thenReturn(2L);

        // when / then
        assertThatThrownBy(() -> getVideoUrlUseCase.getVideoUrl(analysisId, userDetails))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED_TO_ANALYSIS);

        verify(videoStorage, never()).generatePresignedUrl(anyString(), any());
    }

    @Test
    @DisplayName("영상 S3 Key가 없으면 VIDEO_NOT_FOUND 예외가 발생한다.")
    void getVideoUrl_fail_whenVideoS3KeyIsNull() {
        // given
        UUID analysisId = UUID.randomUUID();
        Long userId = 1L;

        Analysis analysis = mock(Analysis.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(analysisRepository.getById(analysisId)).thenReturn(analysis);
        when(analysis.getUserId()).thenReturn(userId);
        when(userDetails.getId()).thenReturn(userId);
        when(analysis.getVideoS3Key()).thenReturn(null);

        // when / then
        assertThatThrownBy(() -> getVideoUrlUseCase.getVideoUrl(analysisId, userDetails))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VIDEO_NOT_FOUND);

        verify(videoStorage, never()).generatePresignedUrl(anyString(), any());
    }

    @Test
    @DisplayName("영상 S3 Key가 빈 문자열이면 VIDEO_NOT_FOUND 예외가 발생한다.")
    void getVideoUrl_fail_whenVideoS3KeyIsBlank() {
        // given
        UUID analysisId = UUID.randomUUID();
        Long userId = 1L;

        Analysis analysis = mock(Analysis.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(analysisRepository.getById(analysisId)).thenReturn(analysis);
        when(analysis.getUserId()).thenReturn(userId);
        when(userDetails.getId()).thenReturn(userId);
        when(analysis.getVideoS3Key()).thenReturn("   ");

        // when / then
        assertThatThrownBy(() -> getVideoUrlUseCase.getVideoUrl(analysisId, userDetails))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VIDEO_NOT_FOUND);

        verify(videoStorage, never()).generatePresignedUrl(anyString(), any());
    }
}
