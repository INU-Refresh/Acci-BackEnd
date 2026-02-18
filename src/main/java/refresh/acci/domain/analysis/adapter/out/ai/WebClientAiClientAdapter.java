package refresh.acci.domain.analysis.adapter.out.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiAnalyzeResponse;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiResultResponse;
import refresh.acci.domain.analysis.adapter.out.ai.dto.res.AiStatusResponse;
import refresh.acci.domain.analysis.application.port.out.AiClientPort;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.nio.file.Path;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebClientAiClientAdapter implements AiClientPort {

    private final WebClient aiWebClient;

    // AI 서버와 통신하여 분석 요청 및 결과 수신
    @Override
    public AiAnalyzeResponse requestAnalysis(Path videoPath) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("video", new FileSystemResource(videoPath.toFile()))
                .filename(videoPath.getFileName().toString())
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

    // AI 서버에서 분석 상태 조회
    @Override
    public AiStatusResponse getStatus(String jobId) {
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
    @Override
    public AiResultResponse getResult(String jobId) {
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
}
