package refresh.acci.domain.repair.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

/**
 * 차량 파손 정도
 */
@Getter
@RequiredArgsConstructor
public enum DamageSeverity {
    SCRATCH("scratch", "스크래치"),
    DENT("dent", "찌그러짐"),
    CRACK("crack", "균열"),
    SEVERE("severe", "반파");

    private final String code;
    private final String displayName;

    public static DamageSeverity from(String code) {
        if (code == null) {
            throw new CustomException(ErrorCode.INVALID_DAMAGE_SEVERITY);
        }

        for (DamageSeverity severity : values()) {
            if (severity.code.equalsIgnoreCase(code)) {
                return severity;
            }
        }
        throw new CustomException(ErrorCode.INVALID_DAMAGE_SEVERITY);
    }
}
