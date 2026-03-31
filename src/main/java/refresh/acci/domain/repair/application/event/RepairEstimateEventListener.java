package refresh.acci.domain.repair.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import refresh.acci.domain.repair.application.service.RepairEstimateWorkerService;
import refresh.acci.domain.repair.application.service.RepairEstimateSseService;

import java.util.UUID;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RepairEstimateEventListener {

    private final RepairEstimateWorkerService repairEstimateWorkerService;
    private final RepairEstimateSseService sseService;
    private final Executor repairEstimateExecutor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRepairEstimateCreated(RepairEstimateCreatedEvent event) {
        UUID estimateId = event.getEstimateId();

        repairEstimateExecutor.execute(() -> {
            try {
                repairEstimateWorkerService.processEstimate(estimateId);
                sseService.sendStatusAndClose(estimateId);
            } catch (Exception e) {
                log.error("수리비 견적 처리 실패 - estimateId: {}", estimateId, e);
                repairEstimateWorkerService.handleFailure(estimateId);
                sseService.sendStatusAndClose(estimateId);
            }
        });
    }
}
