package refresh.acci.domain.repair.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import refresh.acci.domain.repair.model.enums.DamageSeverity;

import java.util.UUID;

/**
 * LLM 요청시 부위별 상태
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "damage_detail")
public class DamageDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repair_estimate_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID repairEstimateId;

    @Column(name = "part_name_kr", nullable = false, length = 100)
    private String partNameKr;

    @Column(name = "part_name_en", nullable = false, length = 100)
    private String partNameEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "damage_severity", nullable = false)
    private DamageSeverity damageSeverity;

    @Column(name = "user_description", length = 300)
    private String userDescription;

    @Builder
    public DamageDetail(UUID repairEstimateId, String partNameKr, String partNameEn, DamageSeverity damageSeverity, String userDescription) {
        this.repairEstimateId = repairEstimateId;
        this.partNameKr = partNameKr;
        this.partNameEn = partNameEn;
        this.damageSeverity = damageSeverity;
        this.userDescription = userDescription;
    }

    public static DamageDetail of(UUID repairEstimateId, String partNameKr, String partNameEn, DamageSeverity damageSeverity, String userDescription) {
        return DamageDetail.builder()
                .repairEstimateId(repairEstimateId)
                .partNameKr(partNameKr)
                .partNameEn(partNameEn)
                .damageSeverity(damageSeverity)
                .userDescription(userDescription)
                .build();
    }
}