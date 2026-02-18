package refresh.acci.domain.analysis.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiResultResponse;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiStatusResponse;
import refresh.acci.domain.analysis.application.port.out.AiClientPort;
import refresh.acci.domain.analysis.application.port.out.AnalysisEventPort;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PollAnalysisUseCase {

    private final AiClientPort aiClient;
    private final UpdateAnalysisStatusUseCase updateAnalysisStatusUseCase;
    private final AnalysisEventPort analysisEvent;

    @Value("${ai.max-attempts}")
    private static final int MAX_ATTEMPTS = 60; // 최대 3분 대기 (3초 간격 * 60회)
    @Value("${ai.interval-ms}")
    private static final long INTERVAL_MS = 3000L;

    public void poll(UUID analysisId, String jobId) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            AiStatusResponse status = aiClient.getStatus(jobId);

            if ("completed".equals(status.status())) {
                AiResultResponse result = aiClient.getResult(jobId);

                Analysis analysis = updateAnalysisStatusUseCase.completeFromAi(analysisId, result);

                analysisEvent.sendStatus(analysis);
                return;
            }

            if ("failed".equals(status.status())) {
                failAnalysis(analysisId);
                return;
            }

            sleep(INTERVAL_MS);
        }

        log.warn("AI 분석 시간 초과 (jobId={})", jobId);
        failAnalysis(analysisId);
    }

    private void failAnalysis(UUID analysisId) {
        Analysis analysis = updateAnalysisStatusUseCase.fail(analysisId);
        analysisEvent.sendStatus(analysis);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.ANALYSIS_INTERRUPTED);
        }
    }
}
