package refresh.acci.domain.repair.infra.llm.dto;

public record ImageData(String base64, String mediaType) {

    public static ImageData of(String base64, String mediaType) {
        return new ImageData(base64, mediaType);
    }
}
