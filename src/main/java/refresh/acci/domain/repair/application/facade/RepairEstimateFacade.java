package refresh.acci.domain.repair.application.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.repair.application.event.RepairEstimateCreatedEvent;
import refresh.acci.domain.repair.application.service.RepairEstimateCommandService;
import refresh.acci.domain.repair.application.service.RepairEstimateQueryService;
import refresh.acci.domain.repair.infra.persistence.DamageDetailRepository;
import refresh.acci.domain.repair.infra.persistence.RepairItemRepository;
import refresh.acci.domain.repair.model.DamageDetail;
import refresh.acci.domain.repair.model.RepairEstimate;
import refresh.acci.domain.repair.model.RepairItem;
import refresh.acci.domain.repair.model.VehicleInfo;
import refresh.acci.domain.repair.model.enums.VehicleBrand;
import refresh.acci.domain.repair.model.enums.VehicleSegment;
import refresh.acci.domain.repair.model.enums.VehicleType;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateRequest;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateResponse;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairEstimateFacade {

    private final RepairEstimateQueryService queryService;
    private final RepairEstimateCommandService commandService;
    private final DamageDetailRepository damageDetailRepository;
    private final RepairItemRepository repairItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 수리비 견적 생성
     * 1. 차량 정보 생성
     * 2. RepairEstimate Entity 저장
     * 3. DamageDetail Entity 저장
     * 4. 비동기 견적 처리 이벤트 발행
     */
    @Transactional
    public RepairEstimateResponse createEstimate(RepairEstimateRequest request, Long userId) {
        //차량 정보 생성
        VehicleInfo vehicleInfo = buildVehicleInfo(request);

        //RepairEstimate Entity 정보 생성 및 저장
        RepairEstimate estimate = RepairEstimate.of(userId, vehicleInfo, request.getUserDescription());
        estimate = commandService.createEstimate(estimate);

        //DamageDetail Entity 생성 및 저장
        List<DamageDetail> damageDetails = commandService.saveDamageDetails(estimate.getId(), request.getDamages());

        //비동기로 견적 처리(이벤트 발행)
        eventPublisher.publishEvent(new RepairEstimateCreatedEvent(estimate.getId()));

        log.info("수리비 견적 요청 생성 - estimateId: {}, userId: {}", estimate.getId(), userId);

        return RepairEstimateResponse.of(estimate, damageDetails, List.of());
    }

    public RepairEstimateResponse getEstimate(UUID estimateId) {
        RepairEstimate estimate = queryService.getEstimateById(estimateId);
        List<DamageDetail> damageDetails = damageDetailRepository.findByRepairEstimateId(estimateId);
        List<RepairItem> repairItems = repairItemRepository.findByRepairEstimateId(estimateId);

        return RepairEstimateResponse.of(estimate, damageDetails, repairItems);
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

}
