package refresh.acci.domain.analysis.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import refresh.acci.domain.analysis.infra.file.TempVideoStore;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.presentation.dto.res.AiAnalyzeResponse;
import refresh.acci.domain.analysis.presentation.dto.res.AiResultResponse;
import refresh.acci.domain.analysis.presentation.dto.res.AiStatusResponse;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisWorkerService {

    private final AnalysisSseService sseService;
    private final AnalysisCommandService analysisCommandService;
    private final WebClient aiWebClient;
    private final TempVideoStore tempVideoStore;

    public void runAnalysis(UUID analysisId, Path tempFilePath) {
        try {
            // AI 서버에 영상 파일 전송 및 분석 결과 수신
            AiAnalyzeResponse response = requestAnalyze(tempFilePath);

            // 분석 결과로 Analysis 엔티티 업데이트
            Analysis analysis = analysisCommandService.markProcessing(analysisId, response.job_id());

            // SSE로 분석 성공 알림 전송
            sseService.send(analysisId, "status",
                    Map.of(
                    "analysisId", analysis.getId(),
                    "status", analysis.getAnalysisStatus(),
                    "isCompleted", analysis.isCompleted()
            ));

            pollUntilComplete(analysisId, response.job_id());
        } catch (Exception e) {
            log.warn("분석 작업 중 오류 발생: {}", e.getMessage());

            try {
                // 분석 실패 처리
                Analysis analysis = analysisCommandService.fail(analysisId);

                // SSE로 분석 실패 알림 전송
                sseService.send(analysisId, "status",
                        Map.of(
                        "analysisId", analysis.getId(),
                        "status", analysis.getAnalysisStatus(),
                        "isCompleted", analysis.isCompleted()
                ));
            } catch (Exception ex) {
                log.warn("분석 실패 처리 중 오류 발생: {}", ex.getMessage());
            }
        } finally {
            // 임시 파일 삭제
            try {
                tempVideoStore.deleteFile(tempFilePath);
            } catch (Exception e) {
                log.warn("임시 파일 삭제 중 오류 발생: {}", e.getMessage());
            }
        }
    }

    // AI 서버와 통신하여 분석 요청 및 결과 수신
    private AiAnalyzeResponse requestAnalyze(Path tempFilePath) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("video", new FileSystemResource(tempFilePath.toFile()))
                .filename(tempFilePath.getFileName().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        return aiWebClient.post()
                .uri("/api/v1/analyze") // AI 서버의 분석 엔드포인트
                .contentType(MediaType.MULTIPART_FORM_DATA) // Content-Type 설정
                .body(BodyInserters.fromMultipartData(builder.build())) // 멀티파트 데이터를 실제 HTTP 요청 바디로 인코딩해서 넣음
                .retrieve() // 응답을 받아옴
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), // retrieve()는 4xx/5xx에서 예외 발생
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.warn("AI 서버와의 통신 중 오류 발생: {}", body);
                                    return Mono.error(new CustomException(ErrorCode.AI_SERVER_COMMUNICATION_FAILED));
                                })) // 오류 상태 처리
                .bodyToMono(AiAnalyzeResponse.class) // 응답 바디를 AnalysisUploadResponse 객체로 변환
                .timeout(Duration.ofSeconds(30)) // 타임아웃 30초
                .block(); // Mono를 동기 방식으로 기다려서 결과를 실제 객체로 꺼냄
    }

    // 분석 상태를 주기적으로 확인하고 완료 시 결과 처리
    private void pollUntilComplete(UUID analysisId, String jobId) {
        int maxAttempts = 100; // 약 5분 (3초 * 100)

        for (int i = 0; i < maxAttempts; i++) {
            AiStatusResponse status = getStatus(jobId);

            if ("completed".equals(status.status())) {
                AiResultResponse result = getResult(jobId);

                Analysis analysis = analysisCommandService.completeFromAi(analysisId, result);

                sseService.send(analysisId, "status", Map.of(
                        "status", analysis.getAnalysisStatus(),
                        "isCompleted", true
                ));
                return;
            }

            if ("failed".equals(status.status())) {
                failAnalysis(analysisId);
                return;
            }

            sleep(3000); // 3초 polling
        }

        log.warn("AI 분석 시간 초과 (jobId={})", jobId);
        failAnalysis(analysisId);
    }

    // AI 서버에서 분석 상태 조회
    private AiStatusResponse getStatus(String jobId) {
        return aiWebClient.get()
                .uri("/api/v1/status/{jobId}", jobId)
                .retrieve()
                .onStatus(
                        s -> s.is4xxClientError() || s.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.warn("AI 상태 조회 실패 (jobId={}): {}", jobId, body);
                                    return Mono.error(
                                            new CustomException(ErrorCode.AI_SERVER_COMMUNICATION_FAILED)
                                    );
                                })
                )
                .bodyToMono(AiStatusResponse.class)
                .timeout(Duration.ofSeconds(10))
                .block();
    }

    // AI 서버에서 분석 결과 조회
    private AiResultResponse getResult(String jobId) {
        return aiWebClient.get()
                .uri("/api/v1/result/{jobId}", jobId)
                .retrieve()
                .onStatus(
                        s -> s.is4xxClientError() || s.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.warn("AI 결과 조회 실패 (jobId={}): {}", jobId, body);
                                    return Mono.error(
                                            new CustomException(ErrorCode.AI_SERVER_COMMUNICATION_FAILED)
                                    );
                                })
                )
                .bodyToMono(AiResultResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();
    }

    // 분석 실패 처리 및 SSE 알림 전송
    private void failAnalysis(UUID analysisId) {
        Analysis analysis = analysisCommandService.fail(analysisId);

        sseService.send(analysisId, "status", Map.of(
                "analysisId", analysis.getId(),
                "status", analysis.getAnalysisStatus(),
                "isCompleted", analysis.isCompleted()
        ));
    }

    // 지정된 시간 동안 스레드 일시정지
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.ANALYSIS_INTERRUPTED);
        }
    }
}
