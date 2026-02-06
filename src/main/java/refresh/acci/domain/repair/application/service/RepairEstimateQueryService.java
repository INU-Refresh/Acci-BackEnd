package refresh.acci.domain.repair.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.repair.infra.persistence.RepairEstimateRepository;
import refresh.acci.domain.repair.model.RepairEstimate;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairEstimateQueryService {

    private final RepairEstimateRepository estimateRepository;

    public RepairEstimate getEstimateById(UUID estimateId) {
        return estimateRepository.findById(estimateId)
                .orElseThrow(() -> {
                    log.warn("수리비 견적을 찾을 수 없습니다. ID: {}", estimateId);
                    return new CustomException(ErrorCode.REPAIR_ESTIMATE_NOT_FOUND);
                });
    }

    public List<RepairEstimate> getRecentEstimates(Long userId) {
        return estimateRepository.findTop3ByUserIdOrderByCreatedAtDesc(userId);
    }
}
