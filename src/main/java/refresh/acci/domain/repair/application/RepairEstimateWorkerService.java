package refresh.acci.domain.repair.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import refresh.acci.domain.repair.infra.llm.RepairEstimateLlmClient;
import refresh.acci.domain.repair.infra.llm.RepairPromptBuilder;
import refresh.acci.domain.repair.infra.llm.dto.RepairEstimateLlmRequest;
import refresh.acci.domain.repair.infra.llm.dto.RepairEstimateLlmResponse;
import refresh.acci.domain.repair.infra.persistence.DamageDetailRepository;
import refresh.acci.domain.repair.infra.persistence.RepairEstimateRepository;
import refresh.acci.domain.repair.infra.persistence.RepairItemRepository;
import refresh.acci.domain.repair.model.DamageDetail;
import refresh.acci.domain.repair.model.RepairEstimate;
import refresh.acci.domain.repair.model.RepairItem;
import refresh.acci.domain.repair.model.VehicleInfo;
import refresh.acci.domain.repair.model.enums.RepairMethod;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;
import refresh.acci.global.util.S3FileService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairEstimateWorkerService {

    private static final Duration PRESIGNED_URL_TTL = Duration.ofMinutes(5);

    private final RepairEstimateRepository estimateRepository;
    private final DamageDetailRepository damageDetailRepository;
    private final RepairItemRepository repairItemRepository;
    private final RepairEstimateLlmClient llmClient;
    private final RepairPromptBuilder promptBuilder;
    private final S3FileService s3FileService;

    @Transactional
    public void processEstimate(UUID estimateId) {
        try {
            //RepairEstimate 조회
            RepairEstimate estimate = getEstimateById(estimateId);

            //PROCESSING 상태로 변경
            estimate.startProcessing();
            estimateRepository.flush();

            //DamageDetail 조회
            List<DamageDetail> damageDetails = damageDetailRepository.findByRepairEstimateId(estimateId);

            //이미지 base64 변환
            List<String> imagesBase64 = resolveImagesBase64(estimate.getImageS3Keys());

            //LLM 호출
            RepairEstimateLlmResponse llmResponse = callLlm(estimate.getVehicleInfo(), damageDetails, imagesBase64);
            log.info("LLM response repairItems: {}", llmResponse.getRepairItems());

            //RepairItem 저장
            saveRepairItems(estimateId, llmResponse.getRepairItems());

            //총 금액 계산
            long totalCost = llmResponse.getRepairItems().stream()
                    .mapToLong(RepairEstimateLlmResponse.RepairItem::getCost)
                    .sum();

            //COMPLETED 상태로 변경
            estimate.completeEstimate(totalCost);
            log.info("수리비 견적 처리 완료 - estimateId: {}, totalEstimate: {}", estimateId, totalCost);

        } catch (Exception e) {
            log.error("수리비 견적 처리 실패 - estimateId: {}", estimateId, e);
            handleFailure(estimateId);
        }
    }

    //S3 키 리스트로 이미지 base64 리스트 변환
    private List<String> resolveImagesBase64(List<String> imageS3Keys) {
        if (imageS3Keys == null || imageS3Keys.isEmpty()) return List.of();

        return imageS3Keys.stream()
                .map(this::resolveImageBase64)
                .filter(base64 -> base64 != null)
                .toList();
    }

    //S3 키 하나를 presigned URL -> 이미지 다운로드 -> base64 변환
    private String resolveImageBase64(String imageS3Key) {
        if (imageS3Key == null || imageS3Key.isBlank()) return null;

        try {
            String presignedUrl = s3FileService.generatePresignedUrl(imageS3Key, PRESIGNED_URL_TTL);
            try (InputStream inputStream = URI.create(presignedUrl).toURL().openStream()) {
                byte[] imageBytes = inputStream.readAllBytes();
                return Base64.getEncoder().encodeToString(imageBytes);
            }
        } catch (IOException e) {
            log.warn("이미지 다운로드 실패 - s3Key: {}, 해당 이미지 제외 후 진행", imageS3Key, e);
            return null;
        }
    }

    //RepairEstimate 조회
    private RepairEstimate getEstimateById(UUID estimateId) {
        return estimateRepository.findById(estimateId)
                .orElseThrow(() -> {
                    log.warn("수리비 견적을 찾을 수 없습니다. ID: {}", estimateId);
                    return new CustomException(ErrorCode.REPAIR_ESTIMATE_NOT_FOUND);
                });
    }

    //LLM 호출
    private RepairEstimateLlmResponse callLlm(VehicleInfo vehicleInfo, List<DamageDetail> damageDetails, List<String> imagesBase64) {
        RepairEstimateLlmRequest llmRequest = promptBuilder.toLlmRequest(vehicleInfo, damageDetails);
        String systemMessage = promptBuilder.buildSystemMessage();
        String userPrompt = promptBuilder.buildUserPrompt(llmRequest);
        return llmClient.call(systemMessage, userPrompt, imagesBase64);
    }

    //RepairItem Entity 저장
    private void saveRepairItems(UUID estimateId, List<RepairEstimateLlmResponse.RepairItem> itemDtos) {
        List<RepairItem> repairItems = itemDtos.stream()
                .map(dto -> RepairItem.of(
                        estimateId,
                        dto.getPartName(),
                        RepairMethod.from(dto.getRepairMethod()),
                        dto.getCost()
                ))
                .toList();

        repairItemRepository.saveAll(repairItems);
    }

    //실패 처리
    @Transactional
    public void handleFailure(UUID estimateId) {
        try {
            RepairEstimate estimate = estimateRepository.findById(estimateId).orElse(null);
            if (estimate != null) {
                estimate.failEstimate();
                log.warn("수리비 견적 실패 처리 완료 - estimateId: {}", estimateId);
            }
        } catch (Exception e) {
            log.error("수리비 견적 실패 처리 중 오류 발생 - estimateId: {}", estimateId, e);
        }
    }
}
