package refresh.acci.domain.vectorDb.presentation.dto.res;

public record LegalChunkHit(
        Long id,
        int accidentType,
        String docName,
        int page,
        String chunkText,
        double distance
) {
}
