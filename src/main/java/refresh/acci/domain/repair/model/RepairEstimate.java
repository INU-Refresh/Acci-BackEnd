package refresh.acci.domain.repair.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import refresh.acci.domain.repair.model.enums.EstimateStatus;
import refresh.acci.global.common.BaseTime;
import refresh.acci.global.common.StringListConverter;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
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

    @Convert(converter = StringListConverter.class)
    @Column(name = "image_s3_keys", columnDefinition = "JSON")
    private List<String> imageS3Keys = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "estimate_status", nullable = false)
    private EstimateStatus estimateStatus;

    @Column(name = "total_estimated_cost")
    private Long totalEstimatedCost;

    @Builder
    public RepairEstimate(Long userId, VehicleInfo vehicleInfo) {
        this.userId = userId;
        this.vehicleInfo = vehicleInfo;
        this.estimateStatus = EstimateStatus.PENDING;
    }

    public static RepairEstimate of(Long userId, VehicleInfo vehicleInfo) {
        return RepairEstimate.builder()
                .userId(userId)
                .vehicleInfo(vehicleInfo)
                .build();
    }

    public void attachImageS3Keys(List<String> s3Keys) {
        if (s3Keys == null || s3Keys.isEmpty()) return;
        if (this.imageS3Keys.size() + s3Keys.size() > 5) {
            throw new CustomException(ErrorCode.REPAIR_ESTIMATE_IMAGE_LIMIT_EXCEEDED);
        }
        this.imageS3Keys.addAll(s3Keys);
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
