package refresh.acci.domain.repair.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RepairEstimateRequest {

    private String vehicleBrand;
    private String vehicleModel;
    private Integer vehicleYear;
    private String vehicleType;
    private String vehicleSegment;

    private List<DamageDto> damages;

    private String userDescription;

    @Getter
    @NoArgsConstructor
    public static class DamageDto {
        private String partNameKr;
        private String partNameEn;
        private String damageSeverity;
    }
}
