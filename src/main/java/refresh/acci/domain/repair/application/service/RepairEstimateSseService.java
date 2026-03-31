package refresh.acci.domain.repair.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import refresh.acci.domain.repair.infra.sse.RepairSseEmitterManager;
import refresh.acci.domain.repair.model.RepairEstimate;
import refresh.acci.domain.repair.model.enums.EstimateStatus;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairEstimateSseService {

    private final RepairSseEmitterManager emitterManager;
    private final RepairEstimateQueryService queryService;


    /**
     * SSE 구독 처리
     * - Emitter 생성 후 현재 상태를 즉시 전송
     * - 이미 COMPLETED/FAILED 된 상태라면 즉시 전송 후 연결 종료
     */
    @Transactional(readOnly = true)
    public SseEmitter subscribe(UUID estimateId) {
        RepairEstimate estimate = queryService.getEstimateById(estimateId);
        SseEmitter emitter = emitterManager.create(estimateId);

        sendStatus(estimate);

        if (isTerminalStatus(estimate.getEstimateStatus())) {
            emitterManager.complete(estimateId);
        }

        return emitter;
    }

    /**
     * 상태 변경 이벤트 전송
     */
    public void sendStatus(RepairEstimate estimate) {
        emitterManager.send(estimate.getId(), "status",
                Map.of(
                    "estimateId", estimate.getId(),
                        "status", estimate.getEstimateStatus(),
                        "isCompleted", estimate.getEstimateStatus() == EstimateStatus.COMPLETED
                ));
    }


    /**
     * 상태 전송 + 연결 종료 (COMPLETED / FAILED 시)
     * - 트랜잭션 커밋 이후 호출되는 상황에서 사용 (EventListener 등)
     */
    @Transactional(readOnly = true)
    public void sendStatusAndClose(UUID estimateId) {
        RepairEstimate estimate = queryService.getEstimateById(estimateId);
        sendStatus(estimate);
        emitterManager.complete(estimateId);
        log.info("SSE 이벤트 전송 및 연결 종료 - estimateId: {}, status: {}", estimateId, estimate.getEstimateStatus());
    }

    private boolean isTerminalStatus(EstimateStatus status) {
        return status == EstimateStatus.COMPLETED || status == EstimateStatus.FAILED;
    }
}
