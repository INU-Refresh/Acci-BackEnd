package refresh.acci.domain.analysis.application.port.out;

import refresh.acci.domain.analysis.adapter.in.web.dto.res.AnalysisSummaryResponse;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.global.common.PageResponse;

import java.util.UUID;

public interface AnalysisRepositoryPort {
    Analysis saveAndFlush(Analysis analysis);
    Analysis getById(UUID id);
    PageResponse<AnalysisSummaryResponse> getUserAnalyses(Long userId, int page, int size);
}