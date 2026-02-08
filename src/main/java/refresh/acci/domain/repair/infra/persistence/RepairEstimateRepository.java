package refresh.acci.domain.repair.infra.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import refresh.acci.domain.repair.model.RepairEstimate;

import java.util.UUID;

public interface RepairEstimateRepository extends JpaRepository<RepairEstimate, UUID> {

    Page<RepairEstimate> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
