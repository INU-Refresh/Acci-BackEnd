package refresh.acci.domain.repair.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

/**
 * 차량 제조사
 */
@Getter
@RequiredArgsConstructor
public enum VehicleBrand {
    HYUNDAI("hyundai", "현대"),
    KIA("kia", "기아"),
    GENESIS("genesis", "제네시스");

    private final String code;
    private final String displayName;

    public static VehicleBrand from(String code) {
        if (code == null) {
            throw new CustomException(ErrorCode.INVALID_VEHICLE_BRAND);
        }

        for (VehicleBrand brand : values()) {
            if (brand.code.equalsIgnoreCase(code)) {
                return brand;
            }
        }
        throw new CustomException(ErrorCode.INVALID_VEHICLE_BRAND);
    }
}
