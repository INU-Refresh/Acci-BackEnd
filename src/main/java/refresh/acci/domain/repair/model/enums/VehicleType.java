package refresh.acci.domain.repair.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

/**
 * 차종
 */
@Getter
@RequiredArgsConstructor
public enum VehicleType {
    SEDAN("sedan", "세단"),
    SUV("suv", "SUV"),
    HATCHBACK("hatchback", "해치백"),
    MPV("mpv", "미니밴"),
    VAN("van", "밴"),
    TRUCK("truck", "트럭");

    private final String code;
    private final String displayName;

    public static VehicleType from(String code) {
        if (code == null) {
            throw new CustomException(ErrorCode.INVALID_VEHICLE_TYPE);
        }

        for (VehicleType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new CustomException(ErrorCode.INVALID_VEHICLE_TYPE);
    }
}
