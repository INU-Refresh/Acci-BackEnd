package refresh.acci.domain.analysis.adapter.in.web.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.model.enums.AnalysisStatus;
import refresh.acci.domain.analysis.model.enums.RagStatus;
import refresh.acci.domain.vectorDb.presentation.dto.res.RagSummaryResponse;

import java.util.UUID;

public record AnalysisResultResponse(

        @Schema(description = "분석 UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID analysisId,

        @Schema(description = "AI Job ID", example = "xxxx-xxxx")
        String aiJobId,

        @Schema(description = "사용자 ID", example = "7")
        Long userId,

        @Schema(description = "AI 분석 결과")
        AccidentAiResultResponse accidentAiResultResponse,

        @Schema(description = "사고 유형")
        AccidentTypeResponse accident_type,

        @Schema(description = "RAG 요약 결과")
        RagSummaryResponse ragSummaryResponse,

        @Schema(description = "분석 상태", example = "COMPLETED")
        AnalysisStatus analysisStatus,

        @Schema(description = "RAG 상태", example = "DONE")
        RagStatus ragStatus,

        @Schema(description = "완료 여부", example = "true")
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
