package refresh.acci.domain.analysis.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import refresh.acci.domain.analysis.adapter.in.web.dto.res.AnalysisResultResponse;
import refresh.acci.domain.analysis.application.port.out.AnalysisRepositoryPort;
import refresh.acci.domain.analysis.application.port.out.LawAndPrecedentPort;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.model.enums.AnalysisStatus;
import refresh.acci.domain.analysis.model.enums.RagStatus;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.domain.vectorDb.application.RagSearchService;
import refresh.acci.domain.vectorDb.application.RagSummaryService;
import refresh.acci.domain.vectorDb.presentation.dto.res.RagInfoResponse;
import refresh.acci.domain.vectorDb.presentation.dto.res.RagSummaryResponse;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AnalysisResultUseCaseTest {

    private AnalysisRepositoryPort analysisRepository;
    private LawAndPrecedentPort lawAndPrecedentPort;
    private RagSearchService ragSearchService;
    private RagSummaryService ragSummaryService;
    private AnalysisResultUseCase analysisResultUseCase;

    @BeforeEach
    void setUp() {
        analysisRepository = mock(AnalysisRepositoryPort.class);
        lawAndPrecedentPort = mock(LawAndPrecedentPort.class);
        ragSearchService = mock(RagSearchService.class);
        ragSummaryService = mock(RagSummaryService.class);

        analysisResultUseCase = new AnalysisResultUseCase(
                analysisRepository,
                lawAndPrecedentPort,
                ragSearchService,
                ragSummaryService);
    }

    @Test
    @DisplayName("분석이 완료되지 않았으면 예외가 발생한다.")
    void getAnalysisResult_fail_whenAnalysisNotCompleted() {
        // Given
        UUID analysisId = UUID.randomUUID();
        Analysis analysis = mock(Analysis.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(analysisRepository.getById(analysisId)).thenReturn(analysis);
        when(analysis.getAnalysisStatus()).thenReturn(AnalysisStatus.PROCESSING);

        // When // Then
        assertThatThrownBy(() -> analysisResultUseCase.getAnalysisResult(analysisId, userDetails))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ANALYSIS_NOT_COMPLETED);
    }

    @Test
    @DisplayName("본인 분석이 아니면 접근 거부 예외가 발생한다.")
    void getAnalysisResult_fail_whenAccessDenied() {
        // Given
        UUID analysisId = UUID.randomUUID();
        Analysis analysis = mock(Analysis.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(analysisRepository.getById(analysisId)).thenReturn(analysis);
        when(analysis.getAnalysisStatus()).thenReturn(AnalysisStatus.COMPLETED);
        when(analysis.getUserId()).thenReturn(1L);
        when(userDetails.getId()).thenReturn(2L);

        // When // Then
        assertThatThrownBy(() -> analysisResultUseCase.getAnalysisResult(analysisId, userDetails))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED_TO_ANALYSIS);
    }

    @Test
    @DisplayName("RAG 상태가 DONE이면 저장된 법령과 판례로 결과를 반환한다.")
    void getAnalysisResult_returnSaved_whenRagDone() {
        // Given
        UUID analysisId = UUID.randomUUID();
        Analysis analysis = mock(Analysis.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(analysisRepository.getById(analysisId)).thenReturn(analysis);
        when(analysis.getId()).thenReturn(analysisId);
        when(analysis.getAnalysisStatus()).thenReturn(AnalysisStatus.COMPLETED);
        when(analysis.getUserId()).thenReturn(1L);
        when(userDetails.getId()).thenReturn(1L);
        when(analysis.getRagStatus()).thenReturn(RagStatus.DONE);

        when(lawAndPrecedentPort.getRelatedLawsByAnalysisId(analysisId)).thenReturn(List.of());
        when(lawAndPrecedentPort.getPrecedentCasesByAnalysisId(analysisId)).thenReturn(List.of());

        // When
        AnalysisResultResponse result = analysisResultUseCase.getAnalysisResult(analysisId, userDetails);

        // Then
        assertThat(result).isNotNull();

        verify(lawAndPrecedentPort).getPrecedentCasesByAnalysisId(analysisId);
        verify(lawAndPrecedentPort).getRelatedLawsByAnalysisId(analysisId);
        verify(analysisRepository, never()).tryMarkRagInProgress(analysisId);
    }

    @Test
    @DisplayName("락 획득 실패 후 최신 상태가 DONE이면 저장된 결과를 반환한다")
    void getAnalysisResult_returnSaved_whenLockFailedButLatestDone() {
        // Given
        UUID analysisId = UUID.randomUUID();
        Analysis firstAnalysis = mock(Analysis.class);
        Analysis latestAnalysis = mock(Analysis.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(analysisRepository.getById(analysisId)).thenReturn(firstAnalysis, latestAnalysis);
        when(firstAnalysis.getAnalysisStatus()).thenReturn(AnalysisStatus.COMPLETED);
        when(firstAnalysis.getUserId()).thenReturn(1L);
        when(userDetails.getId()).thenReturn(1L);
        when(firstAnalysis.getRagStatus()).thenReturn(RagStatus.NONE);

        when(analysisRepository.tryMarkRagInProgress(analysisId)).thenReturn(false);

        when(latestAnalysis.getRagStatus()).thenReturn(RagStatus.DONE);
        when(latestAnalysis.getId()).thenReturn(analysisId);

        when(lawAndPrecedentPort.getRelatedLawsByAnalysisId(analysisId)).thenReturn(List.of());
        when(lawAndPrecedentPort.getPrecedentCasesByAnalysisId(analysisId)).thenReturn(List.of());

        // When
        AnalysisResultResponse result = analysisResultUseCase.getAnalysisResult(analysisId, userDetails);

        // Then
        assertThat(result).isNotNull();

        verify(analysisRepository).tryMarkRagInProgress(analysisId);
        verify(lawAndPrecedentPort).getPrecedentCasesByAnalysisId(analysisId);
        verify(lawAndPrecedentPort).getRelatedLawsByAnalysisId(analysisId);
    }

    @Test
    @DisplayName("락 획득 실패 후 최신 상태도 DONE이 아니면 RAG_ALREADY_PROGRESS 예외가 발생한다")
    void getAnalysisResult_fail_whenLockFailedAndLatestNotDone() {
        // Given
        UUID analysisId = UUID.randomUUID();
        Analysis firstAnalysis = mock(Analysis.class);
        Analysis latestAnalysis = mock(Analysis.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(analysisRepository.getById(analysisId)).thenReturn(firstAnalysis, latestAnalysis);
        when(firstAnalysis.getAnalysisStatus()).thenReturn(AnalysisStatus.COMPLETED);
        when(firstAnalysis.getUserId()).thenReturn(1L);
        when(userDetails.getId()).thenReturn(1L);
        when(firstAnalysis.getRagStatus()).thenReturn(RagStatus.NONE);

        when(analysisRepository.tryMarkRagInProgress(analysisId)).thenReturn(false);

        when(latestAnalysis.getRagStatus()).thenReturn(RagStatus.IN_PROGRESS);

        // When
        assertThatThrownBy(() -> analysisResultUseCase.getAnalysisResult(analysisId, userDetails))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RAG_ALREADY_PROGRESS);
    }

    @Test
    @DisplayName("RAG 요약 결과가 null이면 RAG 실패 처리 후 summary 없이 반환한다")
    void getAnalysisResult_returnWithoutSummary_whenRagSummaryIsNull() {
        // Given
        UUID analysisId = UUID.randomUUID();
        Analysis firstAnalysis = mock(Analysis.class);
        Analysis latestAnalysis = mock(Analysis.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(analysisRepository.getById(analysisId)).thenReturn(firstAnalysis, latestAnalysis);
        when(firstAnalysis.getAnalysisStatus()).thenReturn(AnalysisStatus.COMPLETED);
        when(firstAnalysis.getUserId()).thenReturn(1L);
        when(userDetails.getId()).thenReturn(1L);
        when(firstAnalysis.getRagStatus()).thenReturn(RagStatus.NONE);

        when(analysisRepository.tryMarkRagInProgress(analysisId)).thenReturn(true);

        when(latestAnalysis.getId()).thenReturn(analysisId);

        when(ragSearchService.search(latestAnalysis))
                .thenThrow(new CustomException(ErrorCode.ACCIDENT_TYPE_NOT_DETECTED));

        // When
        AnalysisResultResponse result = analysisResultUseCase.getAnalysisResult(analysisId, userDetails);

        // Then
        assertThat(result).isNotNull();
        verify(analysisRepository).markRagFailed(analysisId);
    }

    @Test
    @DisplayName("RAG 저장에 성공하면 DONE 처리와 요약 저장을 수행한다")
    void getAnalysisResult_success_whenRagSaved() {
        // Given
        UUID analysisId = UUID.randomUUID();
        Analysis firstAnalysis = mock(Analysis.class);
        Analysis latestAnalysis = mock(Analysis.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        RagInfoResponse ragInfoResponse = mock(RagInfoResponse.class);
        RagSummaryResponse ragSummaryResponse = mock(RagSummaryResponse.class);

        when(analysisRepository.getById(analysisId)).thenReturn(firstAnalysis, latestAnalysis);
        when(firstAnalysis.getAnalysisStatus()).thenReturn(AnalysisStatus.COMPLETED);
        when(firstAnalysis.getUserId()).thenReturn(1L);
        when(userDetails.getId()).thenReturn(1L);
        when(firstAnalysis.getRagStatus()).thenReturn(RagStatus.NONE);

        when(analysisRepository.tryMarkRagInProgress(analysisId)).thenReturn(true);

        when(latestAnalysis.getId()).thenReturn(analysisId);
        when(ragSearchService.search(latestAnalysis)).thenReturn(ragInfoResponse);
        when(ragSummaryService.summarize(ragInfoResponse)).thenReturn(ragSummaryResponse);

        when(ragSummaryResponse.accidentSituation()).thenReturn("상황 요약");
        when(ragSummaryResponse.accidentExplain()).thenReturn("상세 설명");

        when(lawAndPrecedentPort.saveRelatedLawsAndPrecedents(analysisId, ragSummaryResponse)).thenReturn(true);

        // When
        AnalysisResultResponse result = analysisResultUseCase.getAnalysisResult(analysisId, userDetails);

        // Then
        assertThat(result).isNotNull();
        verify(analysisRepository).markRagDone(analysisId);
        verify(analysisRepository).setAnalysisSummary(analysisId, "상황 요약", "상세 설명");
        verify(analysisRepository, never()).markRagFailed(analysisId);
    }

    @Test
    @DisplayName("RAG 저장에 실패하면 RAG 실패 처리한다")
    void getAnalysisResult_failSave_whenLawAndPrecedentSaveFails() {
        // Given
        UUID analysisId = UUID.randomUUID();
        Analysis firstAnalysis = mock(Analysis.class);
        Analysis latestAnalysis = mock(Analysis.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        RagInfoResponse ragInfoResponse = mock(RagInfoResponse.class);
        RagSummaryResponse ragSummaryResponse = mock(RagSummaryResponse.class);

        when(analysisRepository.getById(analysisId)).thenReturn(firstAnalysis, latestAnalysis);
        when(firstAnalysis.getAnalysisStatus()).thenReturn(AnalysisStatus.COMPLETED);
        when(firstAnalysis.getUserId()).thenReturn(1L);
        when(userDetails.getId()).thenReturn(1L);
        when(firstAnalysis.getRagStatus()).thenReturn(RagStatus.NONE);

        when(analysisRepository.tryMarkRagInProgress(analysisId)).thenReturn(true);

        when(latestAnalysis.getId()).thenReturn(analysisId);
        when(ragSearchService.search(latestAnalysis)).thenReturn(ragInfoResponse);
        when(ragSummaryService.summarize(ragInfoResponse)).thenReturn(ragSummaryResponse);

        when(lawAndPrecedentPort.saveRelatedLawsAndPrecedents(analysisId, ragSummaryResponse)).thenReturn(false);

        // When
        AnalysisResultResponse result = analysisResultUseCase.getAnalysisResult(analysisId, userDetails);

        // Then
        assertThat(result).isNotNull();
        verify(analysisRepository).markRagFailed(analysisId);
        verify(analysisRepository, never()).markRagDone(analysisId);
    }

    @Test
    @DisplayName("Gemini rate limit 예외가 발생하면 RAG 상태를 NONE으로 되돌리고 예외를 다시 던진다")
    void getAnalysisResult_fail_whenGeminiRateLimited() {
        // Given
        UUID analysisId = UUID.randomUUID();
        Analysis firstAnalysis = mock(Analysis.class);
        Analysis latestAnalysis = mock(Analysis.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        RagInfoResponse ragInfoResponse = mock(RagInfoResponse.class);

        when(analysisRepository.getById(analysisId)).thenReturn(firstAnalysis, latestAnalysis);
        when(firstAnalysis.getAnalysisStatus()).thenReturn(AnalysisStatus.COMPLETED);
        when(firstAnalysis.getUserId()).thenReturn(1L);
        when(userDetails.getId()).thenReturn(1L);
        when(firstAnalysis.getRagStatus()).thenReturn(RagStatus.NONE);

        when(analysisRepository.tryMarkRagInProgress(analysisId)).thenReturn(true);

        when(latestAnalysis.getId()).thenReturn(analysisId);
        when(ragSearchService.search(latestAnalysis)).thenReturn(ragInfoResponse);

        when(ragSummaryService.summarize(ragInfoResponse))
                .thenThrow(new CustomException(ErrorCode.GEMINI_RATE_LIMITED));

        // When // Then
        assertThatThrownBy(() -> analysisResultUseCase.getAnalysisResult(analysisId, userDetails))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GEMINI_RATE_LIMITED);

        verify(analysisRepository).markRagNone(analysisId);
        verify(analysisRepository, never()).markRagFailed(analysisId);
        verify(analysisRepository, never()).markRagDone(analysisId);
    }
}
