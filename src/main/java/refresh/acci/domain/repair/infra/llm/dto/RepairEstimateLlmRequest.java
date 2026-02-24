package refresh.acci.domain.repair.infra.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RepairEstimateLlmRequest {

    @JsonProperty("vehicle_info")
    private VehicleInfo vehicleInfo;

    @JsonProperty("damage_details")
    private List<DamageDetail> damageDetails;

    @Getter
    @Builder
    public static class VehicleInfo {
        private String brand;
        private String model;
        private Integer year;

        @JsonProperty("vehicle_type")
        private String vehicleType;

        private String segment;
    }

    @Getter
    @Builder
    public static class DamageDetail {
        @JsonProperty("part_name_en")
        private String partNameEn;

        @JsonProperty("part_name_kr")
        private String partNameKr;

        @JsonProperty("damage_severity")
        private String damageSeverity;

        @JsonProperty("user_description")
        private String userDescription;
    }
}
