package refresh.acci.domain.repair.application.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
import refresh.acci.domain.repair.presentation.dto.RepairEstimateSummaryResponse;
import refresh.acci.global.common.PageResponse;
import refresh.acci.global.util.S3FileService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairEstimateFacade {

    private final RepairEstimateQueryService queryService;
    private final RepairEstimateCommandService commandService;
    private final DamageDetailRepository damageDetailRepository;
    private final RepairItemRepository repairItemRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final S3FileService s3FileService;

    /**
     * 수리비 견적 생성
     * 1. 차량 정보 생성
     * 2. RepairEstimate Entity 저장
     * 3. DamageDetail Entity 저장
     * 4. 비동기 견적 처리 이벤트 발행
     */
    @Transactional
    public RepairEstimateResponse createEstimate(RepairEstimateRequest request, List<MultipartFile> images, Long userId) {
        //차량 정보 생성
        VehicleInfo vehicleInfo = buildVehicleInfo(request);

        //RepairEstimate Entity 정보 생성 및 저장
        RepairEstimate estimate = RepairEstimate.of(userId, vehicleInfo);
        estimate = commandService.createEstimate(estimate);

        //이미지 S3 업로드
        if (images != null && !images.isEmpty()) {
            uploadImages(estimate, images);
        }

        //DamageDetail Entity 생성 및 저장
        List<DamageDetail> damageDetails = commandService.saveDamageDetails(estimate.getId(), request.getDamages());

        //비동기로 견적 처리(이벤트 발행)
        eventPublisher.publishEvent(new RepairEstimateCreatedEvent(estimate.getId()));

        log.info("수리비 견적 요청 생성 - estimateId: {}, userId: {}", estimate.getId(), userId);

        return RepairEstimateResponse.of(estimate, damageDetails, List.of());
    }

    @Transactional(readOnly = true)
    public RepairEstimateResponse getEstimate(UUID estimateId) {
        RepairEstimate estimate = queryService.getEstimateById(estimateId);
        List<DamageDetail> damageDetails = damageDetailRepository.findByRepairEstimateId(estimateId);
        List<RepairItem> repairItems = repairItemRepository.findByRepairEstimateId(estimateId);

        return RepairEstimateResponse.of(estimate, damageDetails, repairItems);
    }

    @Transactional(readOnly = true)
    public PageResponse<RepairEstimateSummaryResponse> getUserEstimates(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RepairEstimate> estimatePage = queryService.getUserEstimates(userId, pageable);

        //estimateId 리스트 추출
        List<UUID> estimateIds = estimatePage.getContent().stream()
                .map(RepairEstimate::getId)
                .toList();

        //한 번에 모든 DamageDetail 조회 후 Map으로 그룹핑
        Map<UUID, List<DamageDetail>> damageDetailMap = damageDetailRepository
                .findByRepairEstimateIdIn(estimateIds)
                .stream()
                .collect(Collectors.groupingBy(DamageDetail::getRepairEstimateId));

        //RepairEstimate를 SummaryResponse로 변환
        Page<RepairEstimateSummaryResponse> responsePage = estimatePage.map(estimate -> {
            List<DamageDetail> damageDetails = damageDetailMap.getOrDefault(estimate.getId(), Collections.emptyList());
            return RepairEstimateSummaryResponse.of(estimate, damageDetails);
        });

        return PageResponse.of(responsePage);
    }

    //이미지 S3 업로드 후 엔티티에 S3 키 저장
    private void uploadImages(RepairEstimate estimate, List<MultipartFile> images) {
        List<String> s3Keys = images.stream()
                .filter(image -> image != null && !image.isEmpty())
                .map(image -> s3FileService.uploadMultipartFile("repair-estimate/" + estimate.getId(), image))
                .toList();

        estimate.attachImageS3Keys(s3Keys);
        log.info("이미지 S3 업로드 완료 - estimateId: {}, s3Keys: {}", estimate.getId(), s3Keys);
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
