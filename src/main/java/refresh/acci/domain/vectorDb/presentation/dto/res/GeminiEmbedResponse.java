package refresh.acci.domain.vectorDb.presentation.dto.res;

public record GeminiEmbedResponse(Embedding embedding) {
    public record Embedding(double[] values) {}
}