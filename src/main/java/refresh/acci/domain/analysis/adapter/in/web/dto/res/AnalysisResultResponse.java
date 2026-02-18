package refresh.acci.domain.analysis.adapter.in.web.dto.res;

import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.model.enums.AnalysisStatus;

import java.util.UUID;

public record AnalysisResultResponse(

        UUID analysisId,

        String aiJobId,

        Long userId,

        AccidentTypeResponse accident_type,

        Long vehicle_A_fault,

        Long vehicle_B_fault,

        String place,

        String situation,

        String vehicle_a,

        String vehicle_b,

        AnalysisStatus analysisStatus,

        boolean isCompleted

) {
    public static AnalysisResultResponse of(Analysis analysis) {
        return new AnalysisResultResponse(
                analysis.getId(),
                analysis.getAiJobId(),
                analysis.getUserId(),
                AccidentTypeResponse.of(analysis.getAccidentType()),
                analysis.getAccidentRateA(),
                analysis.getAccidentRateB(),
                analysis.getPlace(),
                analysis.getSituation(),
                analysis.getVehicleASituation(),
                analysis.getVehicleBSituation(),
                analysis.getAnalysisStatus(),
                analysis.isCompleted()
        );
    }
}
