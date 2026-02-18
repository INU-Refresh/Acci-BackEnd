package refresh.acci.domain.analysis.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiAnalyzeResponse;
import refresh.acci.domain.analysis.application.port.out.AiClientPort;
import refresh.acci.domain.analysis.application.port.out.AnalysisEventPort;
import refresh.acci.domain.analysis.application.port.out.TempFilePort;
import refresh.acci.domain.analysis.model.Analysis;

import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunAnalysisUseCase {

    private final AnalysisEventPort analysisEvent;
    private final UpdateAnalysisStatusUseCase updateAnalysisStatusUseCase;
    private final AiClientPort aiClient;
    private final PollAnalysisUseCase pollAnalysisUseCase;
    private final TempFilePort tempFilePort;

    public void runAnalysis(UUID analysisId, Path tempFilePath) {
        try {
            // AI 서버에 영상 파일 전송 및 분석 결과 수신
            AiAnalyzeResponse response = aiClient.requestAnalysis(tempFilePath);

            // 분석 결과로 Analysis 엔티티 업데이트
            Analysis analysis = updateAnalysisStatusUseCase.markProcessing(analysisId, response.job_id());

            // SSE로 분석 성공 알림 전송
            analysisEvent.sendStatus(analysis);

            pollAnalysisUseCase.poll(analysisId, response.job_id());
        } catch (Exception e) {
            log.warn("분석 작업 중 오류 발생: {}", e.getMessage());

            try {
                // 분석 실패 처리
                Analysis analysis = updateAnalysisStatusUseCase.fail(analysisId);

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
}
