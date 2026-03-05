package refresh.acci.domain.analysis.application.port.out;

import refresh.acci.domain.vectorDb.presentation.dto.res.RagSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface LawAndPrecedentPort {
    boolean saveRelatedLawsAndPrecedents(UUID analysisId, RagSummaryResponse ragSummaryResponse);
    List<RagSummaryResponse.RelatedLawsResponse> getRelatedLawsByAnalysisId(UUID analysisId);
    List<RagSummaryResponse.PrecedentCasesResponse> getPrecedentCasesByAnalysisId(UUID analysisId);
}
