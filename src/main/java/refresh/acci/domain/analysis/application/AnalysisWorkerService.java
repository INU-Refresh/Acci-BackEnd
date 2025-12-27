package refresh.acci.domain.analysis.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import refresh.acci.domain.analysis.infra.file.TempVideoStore;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.presentation.dto.res.AnalysisResultResponse;
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
    private final WebClient.Builder webClientBuilder;
    private final TempVideoStore tempVideoStore;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public void runAnalysis(UUID analysisId, Path tempFilePath) {
        try {
            // AI 서버에 영상 파일 전송 및 분석 결과 수신
            AnalysisResultResponse response = callAi(tempFilePath);

            // 분석 결과로 Analysis 엔티티 업데이트
            Analysis analysis = analysisCommandService.completeAndGetAnalysis(analysisId, response);

            // SSE로 분석 성공 알림 전송
            sseService.send(analysisId, "status",
                    Map.of(
                    "analysisId", analysis.getId(),
                    "status", analysis.getAnalysisStatus(),
                    "isCompleted", analysis.isCompleted()
            ));
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
    private AnalysisResultResponse callAi(Path tempFilePath) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("video", new FileSystemResource(tempFilePath.toFile()))
                .filename(tempFilePath.getFileName().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl).build();

        return webClient.post()
                .uri("/analyze") // AI 서버의 분석 엔드포인트
                .contentType(MediaType.MULTIPART_FORM_DATA) // Content-Type 설정
                .body(BodyInserters.fromMultipartData(builder.build())) // 멀티파트 데이터를 실제 HTTP 요청 바디로 인코딩해서 넣음
                .retrieve() // 응답을 받아옴
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), // retrieve()는 4xx/5xx에서 예외 발생
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.warn("AI 서버와의 통신 중 오류 발생: {}", body);
                                    return Mono.error(new CustomException(ErrorCode.AI_SERVER_COMMUNICATION_FAILED));
                                })) // 오류 상태 처리
                .bodyToMono(AnalysisResultResponse.class) // 응답 바디를 AnalysisUploadResponse 객체로 변환
                .timeout(Duration.ofMinutes(5)) // 타임아웃 5분
                .block(); // Mono를 동기 방식으로 기다려서 결과를 실제 객체로 꺼냄
    }
}
