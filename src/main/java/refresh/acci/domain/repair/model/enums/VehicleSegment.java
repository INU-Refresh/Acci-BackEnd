package refresh.acci.domain.repair.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

/**
 * 차급
 */
@Getter
@RequiredArgsConstructor
public enum VehicleSegment {
    // 일반 차급
    MINI("mini", "경차"),
    COMPACT("compact", "준중형"),
    MID_SIZE("mid_size", "중형"),
    FULL_SIZE("full_size", "대형"),
    LIGHT_DUTY("light_duty", "소형 상용"),

    // 럭셔리 차급
    LUXURY("luxury", "럭셔리"),
    COMPACT_LUXURY("compact_luxury", "준중형 럭셔리"),
    MID_SIZE_LUXURY("mid_size_luxury", "중형 럭셔리"),
    FULL_SIZE_LUXURY("full_size_luxury", "대형 럭셔리");

    private final String code;
    private final String displayName;

    public static VehicleSegment from(String code) {
        if (code == null) {
            throw new CustomException(ErrorCode.INVALID_VEHICLE_SEGMENT);
        }

        for (VehicleSegment segment : values()) {
            if (segment.code.equalsIgnoreCase(code)) {
                return segment;
            }
        }
        throw new CustomException(ErrorCode.INVALID_VEHICLE_SEGMENT);
    }
}
