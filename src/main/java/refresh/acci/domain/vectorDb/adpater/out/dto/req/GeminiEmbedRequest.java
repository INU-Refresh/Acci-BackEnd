package refresh.acci.domain.vectorDb.adpater.out.dto.req;

public record GeminiEmbedRequest(Content content) {
    public record Content(Part[] parts) {}
    public record Part(String text) {}

    public static GeminiEmbedRequest of(String text) {
        return new GeminiEmbedRequest(new Content(new Part[]{ new Part(text) }));
    }
}
