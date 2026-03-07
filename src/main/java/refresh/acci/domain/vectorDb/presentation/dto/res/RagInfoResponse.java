package refresh.acci.domain.vectorDb.presentation.dto.res;

import java.util.List;

public record RagInfoResponse(

        int accidentType,

        String queryText,

        List<LegalChunkHit> hits
) {
}
