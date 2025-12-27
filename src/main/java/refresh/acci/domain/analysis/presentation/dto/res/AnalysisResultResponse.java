package refresh.acci.domain.analysis.presentation.dto.res;

import refresh.acci.domain.analysis.model.enums.AnalysisStatus;
import refresh.acci.domain.analysis.model.enums.AccidentType;

import java.util.UUID;

public record AnalysisResultResponse(

        UUID id,

        Long userId,

        Long accidentRate,

        AccidentType accidentType,

        AnalysisStatus analysisStatus,

        boolean isCompleted

) {
}
