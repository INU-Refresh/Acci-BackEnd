package refresh.acci.domain.repair.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import refresh.acci.domain.repair.model.RepairEstimate;

import java.util.List;
import java.util.UUID;

public interface RepairEstimateRepository extends JpaRepository<RepairEstimate, UUID> {

    List<RepairEstimate> findByUserId(Long userId);

    List<RepairEstimate> findTop3ByUserIdOrderByCreatedAtDesc(Long userId);
}
