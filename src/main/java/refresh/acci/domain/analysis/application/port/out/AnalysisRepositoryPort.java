package refresh.acci.domain.analysis.application.port.out;

import refresh.acci.domain.analysis.adapter.in.web.dto.res.AnalysisSummaryResponse;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.global.common.PageResponse;

import java.util.UUID;

public interface AnalysisRepositoryPort {
    Analysis saveAndFlush(Analysis analysis);
    Analysis getById(UUID id);
    PageResponse<AnalysisSummaryResponse> getUserAnalyses(Long userId, int page, int size);
    boolean tryMarkRagInProgress(UUID analysisId);
    void markRagDone(UUID analysisId);
    void markRagFailed(UUID analysisId);
    void setAnalysisSummary(UUID analysisId, String accidentSituation, String accidentExplain);
}