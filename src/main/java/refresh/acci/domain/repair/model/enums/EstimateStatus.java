package refresh.acci.domain.repair.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 수리비 LLM 진행 상태
 */
@Getter
@RequiredArgsConstructor
public enum EstimateStatus {
    PENDING("대기"),
    PROCESSING("처리중"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String description;
}
