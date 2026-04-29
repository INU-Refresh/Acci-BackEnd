package refresh.acci.domain.analysis.adapter.in.web.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import refresh.acci.domain.analysis.model.Analysis;
import refresh.acci.domain.analysis.model.enums.AccidentType;

public record AccidentAiResultResponse(

        @Schema(description = "사고 유형", example = "CAR_TO_CAR_REAR_END_PRIMARY")
        AccidentType accidentType,

        @Schema(description = "A의 과실 비율", example = "80")
        Long accidentRateA,

        @Schema(description = "B의 과실 비율", example = "20")
        Long accidentRateB,

        @Schema(description = "사고 장소", example = "직선 도로")
        String place,

        @Schema(description = "사고 상황", example = "추돌 사고")
        String situation,

        @Schema(description = "A 차량 상황", example = "후행 직진")
        String vehicleASituation,

        @Schema(description = "B 차량 상황", example = "선행 진로변경")
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
