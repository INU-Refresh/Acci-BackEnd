package refresh.acci.domain.repair.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import refresh.acci.domain.repair.application.RepairEstimateWorkerService;

import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RepairEstimateEventListener {

    private final RepairEstimateWorkerService repairEstimateWorkerService;
    private final Executor repairEstimateExecutor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRepairEstimateCreated(RepairEstimateCreatedEvent event) {
        repairEstimateExecutor.execute(() ->
                repairEstimateWorkerService.processEstimate(event.getEstimateId())
        );
    }
}
