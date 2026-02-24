package refresh.acci.domain.repair.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import refresh.acci.domain.repair.model.DamageDetail;
import refresh.acci.domain.repair.model.RepairEstimate;
import refresh.acci.domain.repair.model.RepairItem;
import refresh.acci.domain.repair.model.VehicleInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class RepairEstimateResponse {

    private final UUID estimateId;
    private final VehicleInfoDto vehicleInfo;
    private final List<String> images;
    private final List<DamageDetailDto> damageDetails;
    private final List<RepairItemDto> repairItems;
    private final Long totalEstimate;
    private final String status;
    private final LocalDateTime createdAt;


    public static RepairEstimateResponse of(RepairEstimate estimate, List<DamageDetail> damageDetails, List<RepairItem> repairItems, List<String> imageUrls) {
        return RepairEstimateResponse.builder()
                .estimateId(estimate.getId())
                .vehicleInfo(VehicleInfoDto.from(estimate.getVehicleInfo()))
                .images(imageUrls)
                .damageDetails(damageDetails.stream()
                        .map(DamageDetailDto::from)
                        .toList())
                .repairItems(repairItems.stream()
                        .map(RepairItemDto::from)
                        .toList())
                .totalEstimate(estimate.getTotalEstimatedCost())
                .status(estimate.getEstimateStatus().name())
                .createdAt(estimate.getCreatedAt())
                .build();
    }

    @Getter
    @Builder
    public static class VehicleInfoDto {
        private final String brand;
        private final String model;
        private final Integer year;
        private final String vehicleType;
        private final String segment;

        public static VehicleInfoDto from(VehicleInfo info) {
            return VehicleInfoDto.builder()
                    .brand(info.getBrand().getDisplayName())
                    .model(info.getModel())
                    .year(info.getYear())
                    .vehicleType(info.getVehicleType().getDisplayName())
                    .segment(info.getSegment().getDisplayName())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class DamageDetailDto {
        private final String partNameKr;
        private final String partNameEn;
        private final String damageSeverity;
        private final String userDescription;

        public static DamageDetailDto from(DamageDetail damage) {
            return DamageDetailDto.builder()
                    .partNameKr(damage.getPartNameKr())
                    .partNameEn(damage.getPartNameEn())
                    .damageSeverity(damage.getDamageSeverity().getDisplayName())
                    .userDescription(damage.getUserDescription())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class RepairItemDto {
        private final String partName;
        private final String repairMethod;
        private final Long cost;

        public static RepairItemDto from(RepairItem item) {
            return RepairItemDto.builder()
                    .partName(item.getPartName())
                    .repairMethod(item.getRepairMethod().getDisplayName())
                    .cost(item.getCost())
                    .build();
        }
    }
}