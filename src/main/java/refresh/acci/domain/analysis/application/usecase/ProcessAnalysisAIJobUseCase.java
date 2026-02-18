package refresh.acci.domain.analysis.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiAnalyzeResponse;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiResultResponse;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiStatusResponse;
import refresh.acci.domain.analysis.application.port.out.AiClientPort;
import refresh.acci.domain.analysis.application.port.out.AnalysisEventPort;
import refresh.acci.domain.analysis.application.port.out.TempFilePort;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessAnalysisAIJobUseCase {

    private final AnalysisEventPort analysisEvent;
    private final QueryAnalysisUseCase queryAnalysisUseCase;
    private final AiClientPort aiClient;
    private final TempFilePort tempFilePort;

    @Value("${ai.max-attempts}")
    private static final int MAX_ATTEMPTS = 60; // 최대 3분 대기 (3초 간격 * 60회)
    @Value("${ai.interval-ms}")
    private static final long INTERVAL_MS = 3000L;

    public void runAnalysis(UUID analysisId, Path tempFilePath) {
        try {
            // AI 서버에 영상 파일 전송 및 분석 결과 수신
            AiAnalyzeResponse response = aiClient.requestAnalysis(tempFilePath);

            // 분석 결과로 Analysis 엔티티 업데이트
            Analysis analysis = queryAnalysisUseCase.markProcessing(analysisId, response.job_id());

            // SSE로 분석 성공 알림 전송
            analysisEvent.sendStatus(analysis);

            poll(analysisId, response.job_id());
        } catch (Exception e) {
            log.warn("분석 작업 중 오류 발생: {}", e.getMessage());

            try {
                // 분석 실패 처리
                Analysis analysis = queryAnalysisUseCase.fail(analysisId);

                // SSE로 분석 실패 알림 전송
                analysisEvent.sendStatus(analysis);
            } catch (Exception ex) {
                log.warn("분석 실패 처리 중 오류 발생: {}", ex.getMessage());
            }
        } finally {
            // 임시 파일 삭제
            try {
                tempFilePort.deleteTempFile(tempFilePath);
            } catch (Exception e) {
                log.warn("임시 파일 삭제 중 오류 발생: {}", e.getMessage());
            }
        }
    }

    public void poll(UUID analysisId, String jobId) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            AiStatusResponse status = aiClient.getStatus(jobId);

            if ("completed".equals(status.status())) {
                AiResultResponse result = aiClient.getResult(jobId);

                Analysis analysis = queryAnalysisUseCase.completeFromAi(analysisId, result);

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
        Analysis analysis = queryAnalysisUseCase.fail(analysisId);
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
