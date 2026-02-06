package refresh.acci.domain.repair.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.repair.infra.persistence.DamageDetailRepository;
import refresh.acci.domain.repair.infra.persistence.RepairEstimateRepository;
import refresh.acci.domain.repair.model.DamageDetail;
import refresh.acci.domain.repair.model.RepairEstimate;
import refresh.acci.domain.repair.model.enums.DamageSeverity;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateRequest;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairEstimateCommandService {

    private final RepairEstimateRepository estimateRepository;
    private final DamageDetailRepository damageDetailRepository;

    @Transactional
    public RepairEstimate createEstimate(RepairEstimate estimate) {
        return estimateRepository.saveAndFlush(estimate);
    }

    @Transactional
    public List<DamageDetail> saveDamageDetails(java.util.UUID repairEstimateId, List<RepairEstimateRequest.DamageDto> damageDtos) {
        List<DamageDetail> damageDetails = damageDtos.stream()
                .map(dto -> DamageDetail.of(
                        repairEstimateId,
                        dto.getPartNameKr(),
                        dto.getPartNameEn(),
                        DamageSeverity.from(dto.getDamageSeverity())
                ))
                .toList();
        return damageDetailRepository.saveAll(damageDetails);
    }
}
