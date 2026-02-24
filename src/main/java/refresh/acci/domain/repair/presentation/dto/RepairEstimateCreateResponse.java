package refresh.acci.domain.repair.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import refresh.acci.domain.repair.model.RepairEstimate;

import java.util.UUID;

@Getter
@Builder
public class RepairEstimateCreateResponse {

    private final UUID estimateId;
    private final String status;

    public static RepairEstimateCreateResponse from(RepairEstimate estimate) {
        return RepairEstimateCreateResponse.builder()
                .estimateId(estimate.getId())
                .status(estimate.getEstimateStatus().name())
                .build();
    }
}