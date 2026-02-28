package refresh.acci.domain.vectorDb.port.out;

public interface EmbeddingPort {
    float[] embed(String text);
    int dimensions();
}
