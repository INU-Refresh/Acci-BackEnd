package refresh.acci.domain.analysis.adapter.in.web.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import refresh.acci.domain.analysis.model.enums.AccidentType;

public record AccidentTypeResponse(

        @Schema(description = "사고 유형", example = "차대차")
        String objectType,

        @Schema(description = "사고 장소", example = "직선 도로")
        String place,

        @Schema(description = "사고 상황", example = "추돌 사고")
        String situation,

        @Schema(description = "차량 A의 진행 방향", example = "선행자동차(1차사고차량)를 추돌")
        String vehicleADirection

) {
    public static AccidentTypeResponse of(AccidentType accidentType) {
        if (accidentType == null) return null;
        return new AccidentTypeResponse(
                accidentType.getObjectType(),
                accidentType.getPlace(),
                accidentType.getSituation(),
                accidentType.getVehicleADirection()
        );
    }
}
