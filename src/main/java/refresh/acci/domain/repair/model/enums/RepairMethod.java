package refresh.acci.domain.repair.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

/**
 * 수리 방법
 */
@Getter
@RequiredArgsConstructor
public enum RepairMethod {
    REPLACE("replace", "교체"),
    REPAIR("repair", "수리"),
    PAINT("paint", "도색"),
    REPAIR_AND_PAINT("repair_and_paint", "수리+도색");

    private final String code;
    private final String displayName;

    public static RepairMethod from(String code) {
        if (code == null) {
            throw new CustomException(ErrorCode.INVALID_REPAIR_METHOD);
        }

        for (RepairMethod method : values()) {
            if (method.code.equalsIgnoreCase(code)) {
                return method;
            }
        }
        throw new CustomException(ErrorCode.INVALID_REPAIR_METHOD);
    }
}
