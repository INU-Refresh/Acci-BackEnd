package refresh.acci.domain.analysis.adapter.in.web.dto.res;

import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.model.enums.AccidentType;

public record AccidentAiResultResponse(

        AccidentType accidentType,

        Long accidentRateA,

        Long accidentRateB,

        String place,

        String situation,

        String vehicleASituation,

        String vehicleBSituation

) {
    public static AccidentAiResultResponse of(Analysis analysis) {
        return new AccidentAiResultResponse(
                analysis.getAccidentType(),
                analysis.getAccidentRateA(),
                analysis.getAccidentRateB(),
                analysis.getPlace(),
                analysis.getSituation(),
                analysis.getVehicleASituation(),
                analysis.getVehicleBSituation()
        );
    }
}
