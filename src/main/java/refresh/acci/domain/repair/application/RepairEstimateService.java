package refresh.acci.domain.repair.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.repair.application.event.RepairEstimateCreatedEvent;
import refresh.acci.domain.repair.infra.persistence.DamageDetailRepository;
import refresh.acci.domain.repair.infra.persistence.RepairEstimateRepository;
import refresh.acci.domain.repair.infra.persistence.RepairItemRepository;
import refresh.acci.domain.repair.model.DamageDetail;
import refresh.acci.domain.repair.model.RepairEstimate;
import refresh.acci.domain.repair.model.RepairItem;
import refresh.acci.domain.repair.model.VehicleInfo;
import refresh.acci.domain.repair.model.enums.DamageSeverity;
import refresh.acci.domain.repair.model.enums.VehicleBrand;
import refresh.acci.domain.repair.model.enums.VehicleSegment;
import refresh.acci.domain.repair.model.enums.VehicleType;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateRequest;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateResponse;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairEstimateService {

    private final RepairEstimateRepository estimateRepository;
    private final DamageDetailRepository damageDetailRepository;
    private final RepairItemRepository repairItemRepository;
    private final RepairEstimateWorkerService workerService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RepairEstimateResponse createEstimate(RepairEstimateRequest request, Long userId) {
        //차량 정보 생성
        VehicleInfo vehicleInfo = buildVehicleInfo(request);

        //RepairEstimate Entity 정보 생성 및 저장
        RepairEstimate estimate = RepairEstimate.of(userId, vehicleInfo, request.getUserDescription());
        estimateRepository.saveAndFlush(estimate);

        //DamageDetail Entity 생성 및 저장
        List<DamageDetail> damageDetails = saveDamageDetails(estimate.getId(), request.getDamages());

        //비동기로 견적 처리(이벤트 발행)
        eventPublisher.publishEvent(new RepairEstimateCreatedEvent(estimate.getId()));

        log.info("수리비 견적 요청 생성 - estimateId: {}, userId: {}", estimate.getId(), userId);

        return RepairEstimateResponse.from(estimate, damageDetails, List.of());
    }

    public RepairEstimateResponse getEstimate(UUID estimateId) {
        RepairEstimate estimate = getEstimateById(estimateId);
        List<DamageDetail> damageDetails = damageDetailRepository.findByRepairEstimateId(estimateId);
        List<RepairItem> repairItems = repairItemRepository.findByRepairEstimateId(estimateId);

        return RepairEstimateResponse.from(estimate, damageDetails, repairItems);
    }


    //RepairEstimate 조회
    private RepairEstimate getEstimateById(UUID estimateId) {
        return estimateRepository.findById(estimateId)
                .orElseThrow(() -> {
                    log.warn("수리비 견적을 찾을 수 없습니다. ID: {}", estimateId);
                    return new CustomException(ErrorCode.REPAIR_ESTIMATE_NOT_FOUND);
                });
    }

    //차량 정보 생성
    private VehicleInfo buildVehicleInfo(RepairEstimateRequest request) {
        return VehicleInfo.of(
                VehicleBrand.from(request.getVehicleBrand()),
                request.getVehicleModel(),
                request.getVehicleYear(),
                VehicleType.from(request.getVehicleType()),
                VehicleSegment.from(request.getVehicleSegment())
        );
    }

    //DamageDetail Entity 저장
    private List<DamageDetail> saveDamageDetails(UUID repairEstimateId, List<RepairEstimateRequest.DamageDto> damageDtos) {
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
