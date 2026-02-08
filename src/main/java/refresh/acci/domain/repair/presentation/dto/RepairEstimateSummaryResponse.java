package refresh.acci.domain.repair.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import refresh.acci.domain.repair.model.DamageDetail;
import refresh.acci.domain.repair.model.RepairEstimate;
import refresh.acci.domain.repair.model.enums.EstimateStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class RepairEstimateSummaryResponse {
    private final UUID estimateId;
    private final EstimateStatus estimateStatus;
    private final Long totalEstimate;
    private final String vehicleModel;
    private final String damageSummary;
    private final LocalDateTime createdAt;

    public static RepairEstimateSummaryResponse of(RepairEstimate estimate, List<DamageDetail> damageDetails) {
        return RepairEstimateSummaryResponse.builder()
                .estimateId(estimate.getId())
                .estimateStatus(estimate.getEstimateStatus())
                .totalEstimate(estimate.getTotalEstimatedCost())
                .vehicleModel(estimate.getVehicleInfo().getModel())
                .damageSummary(buildDamageSummary(damageDetails))
                .createdAt(estimate.getCreatedAt())
                .build();
    }

    private static String buildDamageSummary(List<DamageDetail> damageDetails) {
        if (damageDetails == null || damageDetails.isEmpty()) {
            return "손상 부위 없음";
        }
        if (damageDetails.size() == 1) {
            return damageDetails.get(0).getPartNameKr();
        }
        String firstPart = damageDetails.get(0).getPartNameKr();
        int remainingCount = damageDetails.size() - 1;
        return String.format("%s 외 %d부위", firstPart, remainingCount);
    }
}
