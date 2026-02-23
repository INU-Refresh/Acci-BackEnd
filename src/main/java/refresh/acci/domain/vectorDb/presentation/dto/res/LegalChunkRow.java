package refresh.acci.domain.vectorDb.presentation.dto.res;

public record LegalChunkRow (
        long id,
        Integer accidentType,
        String docName,
        Integer page,
        String section,
        String caseId,
        String chunkText,
        double distance
){
}