package refresh.acci.domain.analysis.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import refresh.acci.domain.analysis.application.port.out.AnalysisEventPort;
import refresh.acci.domain.analysis.application.port.out.AnalysisRepositoryPort;
import refresh.acci.domain.analysis.model.Analysis;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeAnalysisEventsUseCase {

    private final AnalysisEventPort analysisEvent;
    private final AnalysisRepositoryPort analysisRepository;

    public SseEmitter subscribe(java.util.UUID analysisId) {
        Analysis analysis = analysisRepository.getById(analysisId);

        // SSE 구독
        SseEmitter emitter = analysisEvent.subscribe(analysisId);

        // 구독 성공 메시지(현 상태) 전송
        analysisEvent.sendStatus(analysis);

        return emitter;
    }
}
