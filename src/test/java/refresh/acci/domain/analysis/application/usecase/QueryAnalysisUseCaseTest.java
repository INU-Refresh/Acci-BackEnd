package refresh.acci.domain.analysis.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import refresh.acci.domain.analysis.adapter.in.web.dto.res.AnalysisSummaryResponse;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiResultResponse;
import refresh.acci.domain.analysis.application.port.out.AnalysisRepositoryPort;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.global.common.PageResponse;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class QueryAnalysisUseCaseTest {

    private AnalysisRepositoryPort analysisRepository;
    private QueryAnalysisUseCase queryAnalysisUseCase;

    @BeforeEach
    void setUp() {
        analysisRepository = mock(AnalysisRepositoryPort.class);
        queryAnalysisUseCase = new QueryAnalysisUseCase(analysisRepository);
    }

    @Test
    @DisplayName("사용자의 분석 목록을 조회한다.")
    void getUserAnalyses() {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 10;

        AnalysisSummaryResponse summaryResponse = mock(AnalysisSummaryResponse.class);

        Page<AnalysisSummaryResponse> pageResult = new PageImpl<>(
                List.of(summaryResponse),
                PageRequest.of(page, size),
                1
        );

        PageResponse<AnalysisSummaryResponse> expected = PageResponse.of(pageResult);

        when(analysisRepository.getUserAnalyses(userId, page, size)).thenReturn(expected);

        // When
        PageResponse<AnalysisSummaryResponse> result = queryAnalysisUseCase.getUserAnalyses(userId, page, size);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(analysisRepository).getUserAnalyses(userId, page, size);
    }

    @Test
    @DisplayName("분석을 처리 중 상태로 변경한다.")
    void markProcessing() {
        // Given
        UUID analysisId = UUID.randomUUID();
        String aiJobId = "test-ai-job-id";

        Analysis analysis = mock(Analysis.class);
        when(analysisRepository.getById(analysisId)).thenReturn(analysis);

        // When
        Analysis result = queryAnalysisUseCase.markProcessing(analysisId, aiJobId);

        // Then
        assertThat(result).isEqualTo(analysis);
        verify(analysisRepository).getById(analysisId);
        verify(analysis).markProcessing(aiJobId);
    }

    @Test
    @DisplayName("AI 결과로 분석을 완료 처리한다.")
    void completeFromAi() {
        // Given
        UUID analysisId = UUID.randomUUID();
        AiResultResponse aiResult = mock(AiResultResponse.class);

        Analysis analysis = mock(Analysis.class);
        when(analysisRepository.getById(analysisId)).thenReturn(analysis);

        // When
        Analysis result = queryAnalysisUseCase.completeFromAi(analysisId, aiResult);

        // Then
        assertThat(result).isEqualTo(analysis);
        verify(analysisRepository).getById(analysisId);
        verify(analysis).completeAnalysisFromAi(aiResult);
    }

    @Test
    @DisplayName("분석을 실패 상태로 변경한다.")
    void fail() {
        // Given
        UUID analysisId = UUID.randomUUID();

        Analysis analysis = mock(Analysis.class);
        when(analysisRepository.getById(analysisId)).thenReturn(analysis);

        // When
        Analysis result = queryAnalysisUseCase.fail(analysisId);

        // Then
        assertThat(result).isEqualTo(analysis);
        verify(analysisRepository).getById(analysisId);
        verify(analysis).failAnalysis();
    }
}
