package refresh.acci.domain.vectorDb.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.time.Duration;

@Slf4j
@Component
public class GeminiGenerateClient {

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;

    public GeminiGenerateClient(
            @Qualifier("geminiWebClient") WebClient geminiWebClient,
            ObjectMapper objectMapper) {
        this.geminiWebClient = geminiWebClient;
        this.objectMapper = objectMapper;
    }

    @Value("${gemini.api-key}")
    private String apiKey;

    /**
     * prompt를 보내고, 모델이 출력한 텍스트를 반환
     */
    public String generateText(String prompt) {
        String model = "models/gemini-flash-latest";
        String uri = "/v1beta/" + model + ":generateContent?key=" + apiKey;

        // 요청 바디: contents -> parts -> text
        var body = objectMapper.createObjectNode();
        var contents = body.putArray("contents");
        var content = contents.addObject();
        var parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        String rawJson = geminiWebClient.post()
                .uri(uri)
                .bodyValue(body)
                .exchangeToMono(resp -> resp.bodyToMono(String.class).defaultIfEmpty("")
                        .flatMap(respBody -> {
                            if (resp.statusCode().value() == 429) {
                                String retryAfter = resp.headers().asHttpHeaders().getFirst("Retry-After");
                                log.warn("Gemini 429. retryAfter={}, body={}", retryAfter, respBody);
                                return Mono.error(new CustomException(ErrorCode.GEMINI_RATE_LIMITED));
                            }
                            if (resp.statusCode().is4xxClientError() || resp.statusCode().is5xxServerError()) {
                                log.error("Gemini failed. status={}, body={}", resp.statusCode(), respBody);
                                return Mono.error(new CustomException(ErrorCode.GEMINI_FAILED));
                            }
                            return Mono.just(respBody);
                        })
                )
                .timeout(Duration.ofSeconds(30))
                .block();

        if (rawJson == null || rawJson.isBlank()) {
            throw new CustomException(ErrorCode.GEMINI_RESPONSE_EMPTY);
        }

        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode textNode = root.at("/candidates/0/content/parts/0/text");
            if (textNode.isMissingNode()) {
                throw new RuntimeException("생성된 텍스트를 찾을 수 없습니다. 원본 JSON: " + rawJson);
            }
            return textNode.asText();
        } catch (Exception e) {
            throw new RuntimeException("Gemini API 응답 파싱 실패: " + e.getMessage() + ". 원본 JSON: " + rawJson, e);
        }
    }

    /**
     * 모델 텍스트에서 JSON 객체 부분만 뽑아서 반환
     */
    public String extractJsonObject(String modelText) {
        if (modelText == null) return null;

        // ```json ... ``` 같은 코드블록 제거
        String s = modelText.trim();
        if (s.startsWith("```")) {
            s = s.replaceAll("^```[a-zA-Z]*\\s*", "");
            s = s.replaceAll("\\s*```$", "");
            s = s.trim();
        }

        // 첫 '{'부터 마지막 '}'까지 잘라내기
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1).trim();
        }
        return s;
    }

    public <T> T parseJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패: " + type.getSimpleName() + ": " + e.getMessage() , e);
        }
    }
}
