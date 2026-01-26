package refresh.acci.domain.repair.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import refresh.acci.domain.repair.model.DamageDetail;

import java.util.List;
import java.util.UUID;

public interface DamageDetailRepository extends JpaRepository<DamageDetail, Long> {
    //수리비 견적 ID로 손상 내역 조회
    List<DamageDetail> findByRepairEstimateId(UUID repairEstimateId);
}
