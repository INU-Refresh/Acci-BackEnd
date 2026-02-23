package refresh.acci.domain.repair.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import refresh.acci.domain.repair.model.enums.EstimateStatus;
import refresh.acci.global.common.BaseTime;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "repair_estimate")
public class RepairEstimate extends BaseTime {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Embedded
    private VehicleInfo vehicleInfo;

    @Column(name = "user_description", length = 1000)
    private String userDescription;

    @Column(name = "image_s3_key")
    private String imageS3Key;

    @Enumerated(EnumType.STRING)
    @Column(name = "estimate_status", nullable = false)
    private EstimateStatus estimateStatus;

    @Column(name = "total_estimated_cost")
    private Long totalEstimatedCost;

    @Builder
    public RepairEstimate(Long userId, VehicleInfo vehicleInfo, String userDescription) {
        this.userId = userId;
        this.vehicleInfo = vehicleInfo;
        this.userDescription = userDescription;
        this.estimateStatus = EstimateStatus.PENDING;
    }

    public static RepairEstimate of(Long userId, VehicleInfo vehicleInfo, String userDescription) {
        return RepairEstimate.builder()
                .userId(userId)
                .vehicleInfo(vehicleInfo)
                .userDescription(userDescription)
                .build();
    }

    public void attachImageS3Key(String imageS3Key) {
        this.imageS3Key = imageS3Key;
    }

    public void startProcessing() {
        this.estimateStatus = EstimateStatus.PROCESSING;
    }

    public void completeEstimate(Long totalCost) {
        this.totalEstimatedCost = totalCost;
        this.estimateStatus = EstimateStatus.COMPLETED;
    }

    public void failEstimate() {
        this.estimateStatus = EstimateStatus.FAILED;
    }
}
