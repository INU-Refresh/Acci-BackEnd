package refresh.acci.domain.repair.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import refresh.acci.domain.repair.model.RepairItem;

import java.util.List;
import java.util.UUID;

public interface RepairItemRepository extends JpaRepository<RepairItem, Long> {

    List<RepairItem> findByRepairEstimateId(UUID repairEstimateId);
}
