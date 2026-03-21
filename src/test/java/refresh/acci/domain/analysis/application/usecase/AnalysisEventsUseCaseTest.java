package refresh.acci.domain.analysis.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import refresh.acci.domain.analysis.application.port.out.AnalysisEventPort;
import refresh.acci.domain.analysis.application.port.out.AnalysisRepositoryPort;
import refresh.acci.domain.analysis.model.Analysis;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AnalysisEventsUseCaseTest {

    private AnalysisEventPort analysisEvent;
    private AnalysisRepositoryPort analysisRepository;
    private AnalysisEventsUseCase analysisEventsUseCase;

    @BeforeEach
    void setUp() {
        analysisEvent = mock(AnalysisEventPort.class);
        analysisRepository = mock(AnalysisRepositoryPort.class);
        analysisEventsUseCase = new AnalysisEventsUseCase(analysisEvent, analysisRepository);
    }

    @Test
    @DisplayName("분석 SSE 구독 시, SSEEmitter를 반환하고 현재 상태를 전송한다.")
    void subscribe() {
        // given
        UUID analysisId = UUID.randomUUID();
        Analysis analysis = mock(Analysis.class);
        SseEmitter emitter = new SseEmitter();

        when(analysisRepository.getById(analysisId)).thenReturn(analysis);
        when(analysisEvent.subscribe(analysisId)).thenReturn(emitter);

        // when
        SseEmitter result = analysisEventsUseCase.subscribe(analysisId);

        // then
        assertThat(result).isEqualTo(emitter);

        verify(analysisRepository).getById(analysisId);
        verify(analysisEvent).subscribe(analysisId);
        verify(analysisEvent).sendStatus(analysis);
    }
}
