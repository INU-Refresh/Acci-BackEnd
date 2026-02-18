package refresh.acci.domain.analysis.adapter.in.web.dto.res;

import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.model.enums.AnalysisStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnalysisSummaryResponse(
        UUID analysisId,
        AnalysisStatus analysisStatus,
        boolean isCompleted,
        Long accidentRateA,
        Long accidentRateB,
        LocalDateTime createdAt
) {
    public static AnalysisSummaryResponse from(Analysis analysis) {
        return new AnalysisSummaryResponse(
                analysis.getId(),
                analysis.getAnalysisStatus(),
                analysis.isCompleted(),
                analysis.getAccidentRateA(),
                analysis.getAccidentRateB(),
                analysis.getCreatedAt()
        );
    }
}
