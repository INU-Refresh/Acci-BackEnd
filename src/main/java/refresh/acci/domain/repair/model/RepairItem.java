package refresh.acci.domain.repair.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import refresh.acci.domain.repair.model.enums.RepairMethod;

import java.util.UUID;

/**
 * LLM 응답 부위별 수리비
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "repair_item")
public class RepairItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repair_estimate_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID repairEstimateId;

    @Column(name = "part_name", nullable = false, length = 100)
    private String partName;

    @Enumerated(EnumType.STRING)
    @Column(name = "repair_method", nullable = false)
    private RepairMethod repairMethod;

    @Column(name = "total_cost", nullable = false)
    private Long totalCost;

    @Builder
    public RepairItem(UUID repairEstimateId, String partName, RepairMethod repairMethod, Long totalCost) {
        this.repairEstimateId = repairEstimateId;
        this.partName = partName;
        this.repairMethod = repairMethod;
        this.totalCost = totalCost;
    }

    public static RepairItem of(UUID repairEstimateId, String partName, RepairMethod repairMethod, Long totalCost) {
        return RepairItem.builder()
                .repairEstimateId(repairEstimateId)
                .partName(partName)
                .repairMethod(repairMethod)
                .totalCost(totalCost)
                .build();
    }
}
