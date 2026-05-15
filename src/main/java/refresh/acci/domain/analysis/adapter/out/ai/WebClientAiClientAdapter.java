package refresh.acci.domain.analysis.adapter.out.ai;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class WebClientAiClientAdapter implements AiClientPort {

    // Resilience4j 인스턴스 이름 — application.yml 의 resilience4j.*.instances.aiClient 와 매핑
    private static final String AI_CLIENT = "aiClient";

    private final WebClient aiWebClient;

    public WebClientAiClientAdapter(@Qualifier("aiWebClient") WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }

    /**
     * 적용 순서 (Spring AOP 기본 우선순위 기준, 외->내):
     *   Retry → CircuitBreaker → Bulkhead → 실제 호출
     *
     * - Retry: 일시 장애 시 지수 백오프 재시도 (최대 3회, 500ms → 1000ms ± jitter)
     *          단, ignoreExceptions 로 설정된 CB open / Bulkhead full 예외는 즉시 전파
     * - CircuitBreaker: 실패율 50% 초과 시 OPEN 전환, OPEN 시 CallNotPermittedException 발생
     *                   fallback 에서 원본 예외를 그대로 재throw → Retry 가 ignoreExceptions 처리
     * - Bulkhead: 동시 호출 상한(10) 초과 시 BulkheadFullException 발생
     *             CB ignoreExceptions 에 포함 → 서킷 실패 카운트 없이 즉시 전파
     */
    @Bulkhead(name = AI_CLIENT)
    @CircuitBreaker(name = AI_CLIENT, fallbackMethod = "requestAnalysisFallback")
    @Retry(name = AI_CLIENT)
    @Override
    public AiAnalyzeResponse requestAnalysis(Path videoPath) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("video", new FileSystemResource(videoPath.toFile()))
                .filename(videoPath.getFileName().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        return aiWebClient.post()
                .uri("/api/v1/analyze")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.warn("AI 서버와의 통신 중 오류 발생: {}", body);
                                    return Mono.error(new CustomException(ErrorCode.AI_SERVER_COMMUNICATION_FAILED));
                                }))
                .bodyToMono(AiAnalyzeResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();
    }

    @Bulkhead(name = AI_CLIENT)
    @CircuitBreaker(name = AI_CLIENT, fallbackMethod = "getStatusFallback")
    @Retry(name = AI_CLIENT)
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
                                    return Mono.error(new CustomException(ErrorCode.AI_SERVER_COMMUNICATION_FAILED));
                                })
                )
                .bodyToMono(AiStatusResponse.class)
                .timeout(Duration.ofSeconds(10))
                .block();
    }

    @Bulkhead(name = AI_CLIENT)
    @CircuitBreaker(name = AI_CLIENT, fallbackMethod = "getResultFallback")
    @Retry(name = AI_CLIENT)
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
                                    return Mono.error(new CustomException(ErrorCode.AI_SERVER_COMMUNICATION_FAILED));
                                })
                )
                .bodyToMono(AiResultResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();
    }

    // ── Fallback methods ────────────────────────────────────────────────────────
    // CircuitBreaker OPEN 또는 실패 시 호출됨.
    // CallNotPermittedException / BulkheadFullException 은 원본 그대로 재throw 해야
    // Retry 의 ignoreExceptions 설정이 동작하여 재시도 없이 즉시 전파됨.
    // CustomException 으로 감싸면 Retry 가 이를 재시도 대상으로 인식하는 문제 발생.
    private AiAnalyzeResponse requestAnalysisFallback(Path videoPath, Throwable t) {
        throw buildFallbackException("requestAnalysis", t);
    }

    private AiStatusResponse getStatusFallback(String jobId, Throwable t) {
        throw buildFallbackException("getStatus", t);
    }

    private AiResultResponse getResultFallback(String jobId, Throwable t) {
        throw buildFallbackException("getResult", t);
    }

    private RuntimeException buildFallbackException(String method, Throwable t) {
        if (t instanceof CallNotPermittedException cpe) {
            // 서킷이 OPEN 상태 — 원본 예외 재throw 로 Retry 의 ignoreExceptions 처리
            log.warn("[AIClient] 서킷 브레이커 OPEN — 즉시 실패 (method={})", method);
            return cpe;
        }
        if (t instanceof BulkheadFullException bfe) {
            // Bulkhead 포화 — CB ignoreExceptions 를 통과해서 여기까지 오는 경우 방어
            log.warn("[AIClient] Bulkhead 포화 — 즉시 실패 (method={})", method);
            return bfe;
        }
        log.warn("[AIClient] AI 서버 호출 실패 (method={}, cause={})", method, t.getMessage());
        return new CustomException(ErrorCode.AI_SERVER_COMMUNICATION_FAILED);
    }
}
