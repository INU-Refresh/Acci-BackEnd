package refresh.acci.domain.vectorDb.adpater.out;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import refresh.acci.domain.vectorDb.adpater.out.dto.req.GeminiEmbedRequest;
import refresh.acci.domain.vectorDb.adpater.out.dto.res.GeminiEmbedResponse;
import refresh.acci.domain.vectorDb.port.out.EmbeddingPort;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

@Slf4j
@Component
public class GeminiEmbeddingAdapter implements EmbeddingPort {

    private final WebClient geminiWebClient;

    public GeminiEmbeddingAdapter(@Qualifier("geminiWebClient") WebClient geminiWebClient) {
        this.geminiWebClient = geminiWebClient;
    }

    @Value("${gemini.api-key}")
    private String apiKey;

    @Override
    public int dimensions() {
        return 3072;
    }

    @Override
    public float[] embed(String text) {
        GeminiEmbedResponse res = geminiWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1beta/models/gemini-embedding-001:embedContent")
                        .queryParam("key", apiKey)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(GeminiEmbedRequest.of(text))
                .retrieve()
                .onStatus(s -> s.value() == 429, r -> r.bodyToMono(String.class).flatMap(b -> {
                    log.warn("Gemini rate limited: {}", b);
                    return Mono.error(new RuntimeException("GEMINI_RATE_LIMIT"));
                }))
                .onStatus(HttpStatusCode::isError, r -> r.bodyToMono(String.class).flatMap(b -> {
                    log.warn("Gemini embed error: {}", b);
                    return Mono.error(new RuntimeException("GEMINI_EMBED_FAILED"));
                }))
                .bodyToMono(GeminiEmbedResponse.class)
                .block();

        if (res == null || res.embedding() == null || res.embedding().values() == null) {
            throw new CustomException(ErrorCode.GEMINI_EMBED_FAILED);
        }

        double[] v = res.embedding().values();
        float[] out = new float[v.length];
        for (int i = 0; i < v.length; i++) out[i] = (float) v[i];

        if (out.length != 3072) {
            log.warn("Embedding dimension mismatch: {}", out.length);
        }
        return out;
    }
}
