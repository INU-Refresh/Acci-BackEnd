package refresh.acci.domain.repair.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import refresh.acci.domain.repair.model.enums.VehicleBrand;
import refresh.acci.domain.repair.model.enums.VehicleSegment;
import refresh.acci.domain.repair.model.enums.VehicleType;

/**
 * 차량 정보
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VehicleInfo {

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_brand", nullable = false)
    private VehicleBrand brand;

    @Column(name = "vehicle_model", nullable = false, length = 50)
    private String model;

    @Column(name = "vehicle_year", nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_segment", nullable = false)
    private VehicleSegment segment;

    @Builder
    public VehicleInfo(VehicleBrand brand, String model, Integer year, VehicleType vehicleType, VehicleSegment segment) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.vehicleType = vehicleType;
        this.segment = segment;
    }

    public static VehicleInfo of(VehicleBrand brand, String model, Integer year, VehicleType vehicleType, VehicleSegment segment) {
        return VehicleInfo.builder()
                .brand(brand)
                .model(model)
                .year(year)
                .vehicleType(vehicleType)
                .segment(segment)
                .build();
    }
}
