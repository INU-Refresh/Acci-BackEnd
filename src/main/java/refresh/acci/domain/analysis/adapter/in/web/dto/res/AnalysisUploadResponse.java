package refresh.acci.domain.analysis.adapter.in.web.dto.res;

import refresh.acci.domain.analysis.model.enums.AnalysisStatus;
import refresh.acci.domain.analysis.model.Analysis;

import java.util.UUID;

public record AnalysisUploadResponse(

        UUID analysisId,

        String aiJobId,

        AnalysisStatus analysisStatus,

        boolean isCompleted

) {
    public static AnalysisUploadResponse of(Analysis analysis) {
        return new AnalysisUploadResponse(
                analysis.getId(),
                analysis.getAiJobId(),
                analysis.getAnalysisStatus(),
                analysis.isCompleted()
        );
    }
}
