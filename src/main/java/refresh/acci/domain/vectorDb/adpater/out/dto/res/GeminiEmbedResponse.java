package refresh.acci.domain.vectorDb.adpater.out.dto.res;

public record GeminiEmbedResponse(Embedding embedding) {
    public record Embedding(double[] values) {}
}