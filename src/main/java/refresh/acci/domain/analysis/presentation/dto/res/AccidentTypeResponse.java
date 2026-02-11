package refresh.acci.domain.analysis.presentation.dto.res;

import refresh.acci.domain.analysis.model.enums.AccidentType;

public record AccidentTypeResponse(

        String objectType,

        String place,

        String situation,

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
