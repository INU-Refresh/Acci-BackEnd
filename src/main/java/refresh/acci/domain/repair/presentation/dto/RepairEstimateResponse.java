package refresh.acci.domain.repair.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import refresh.acci.domain.repair.model.RepairEstimate;
import refresh.acci.domain.repair.model.DamageDetail;
import refresh.acci.domain.repair.model.RepairItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class RepairEstimateResponse {

    private UUID estimateId;

    private VehicleInfoDto vehicleInfo;

    private List<DamageDetailDto> damageDetails;

    private List<RepairItemDto> repairItems;

    private Long totalEstimate;

    private String status;

    private String userDescription;

    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class VehicleInfoDto {
        private String brand;
        private String model;
        private Integer year;
        private String vehicleType;
        private String segment;
    }

    @Getter
    @Builder
    public static class DamageDetailDto {
        private String partNameKr;
        private String partNameEn;
        private String damageSeverity;
    }

    @Getter
    @Builder
    public static class RepairItemDto {
        private String partName;
        private String repairMethod;
        private Long cost;
    }


    public static RepairEstimateResponse from(RepairEstimate estimate, List<DamageDetail> damageDetails, List<RepairItem> repairItems) {
        return RepairEstimateResponse.builder()
                .estimateId(estimate.getId())
                .vehicleInfo(VehicleInfoDto.builder()
                        .brand(estimate.getVehicleInfo().getBrand().getDisplayName())
                        .model(estimate.getVehicleInfo().getModel())
                        .year(estimate.getVehicleInfo().getYear())
                        .vehicleType(estimate.getVehicleInfo().getVehicleType().getDisplayName())
                        .segment(estimate.getVehicleInfo().getSegment().getDisplayName())
                        .build())
                .damageDetails(damageDetails.stream()
                        .map(damage -> DamageDetailDto.builder()
                                .partNameKr(damage.getPartNameKr())
                                .partNameEn(damage.getPartNameEn())
                                .damageSeverity(damage.getDamageSeverity().getDisplayName())
                                .build())
                        .toList())
                .repairItems(repairItems.stream()
                        .map(item -> RepairItemDto.builder()
                                .partName(item.getPartName())
                                .repairMethod(item.getRepairMethod().getDisplayName())
                                .cost(item.getCost())
                                .build())
                        .toList())
                .totalEstimate(estimate.getTotalEstimatedCost())
                .status(estimate.getEstimateStatus().name())
                .userDescription(estimate.getUserDescription())
                .createdAt(estimate.getCreatedAt())
                .build();
    }
}
