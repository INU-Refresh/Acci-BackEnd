package refresh.acci.domain.vectorDb.presentation.dto.res;

import java.util.List;

public record RagInfoResponse(

        int accidentType,

        String queryText,

        List<LegalChunkHit> hits
) {
    public record LegalChunkHit(
            Long id,
            int accidentType,
            String docName,
            int page,
            String chunkText,
            double distance
    ) {}
}
