package refresh.acci.domain.analysis.adapter.in.web.dto.res;

import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.model.enums.AnalysisStatus;
import refresh.acci.domain.analysis.model.enums.RagStatus;
import refresh.acci.domain.vectorDb.presentation.dto.res.RagSummaryResponse;

import java.util.UUID;

public record AnalysisResultResponse(

        UUID analysisId,

        String aiJobId,

        Long userId,

        AccidentAiResultResponse accidentAiResultResponse,

        AccidentTypeResponse accident_type,

        RagSummaryResponse ragSummaryResponse,

        AnalysisStatus analysisStatus,

        RagStatus ragStatus,

        boolean isCompleted

) {
    public static AnalysisResultResponse of(Analysis analysis, AccidentAiResultResponse accidentAiResultResponse, RagSummaryResponse ragSummaryResponse) {
        return new AnalysisResultResponse(
                analysis.getId(),
                analysis.getAiJobId(),
                analysis.getUserId(),
                accidentAiResultResponse,
                AccidentTypeResponse.of(analysis.getAccidentType()),
                ragSummaryResponse,
                analysis.getAnalysisStatus(),
                analysis.getRagStatus(),
                analysis.isCompleted()
        );
    }
}
