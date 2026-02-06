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
import refresh.acci.domain.repair.model.enums.RepairMethod;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairEstimateWorkerService {

    private final RepairEstimateRepository estimateRepository;
    private final DamageDetailRepository damageDetailRepository;
    private final RepairItemRepository repairItemRepository;
    private final RepairEstimateLlmClient llmClient;
    private final RepairPromptBuilder promptBuilder;

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

            //LLM 호출
            RepairEstimateLlmResponse llmResponse = callLlm(estimate.getVehicleInfo(), damageDetails, estimate.getUserDescription());

            //RepairItem 저장
            saveRepairItems(estimateId, llmResponse.getRepairItems());

            //COMPLETED 상태로 변경
            estimate.completeEstimate(llmResponse.getTotalCost());

            log.info("수리비 견적 처리 완료 - estimateId: {}, totalEstimate: {}", estimateId, llmResponse.getTotalCost());

        } catch (Exception e) {
            log.error("수리비 견적 처리 실패 - estimateId: {}", estimateId, e);
            handleFailure(estimateId);
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
    private RepairEstimateLlmResponse callLlm(refresh.acci.domain.repair.model.VehicleInfo vehicleInfo, List<DamageDetail> damageDetails, String userDescription) {

        //LLM 요청 DTO 생성
        RepairEstimateLlmRequest llmRequest = promptBuilder.toLlmRequest(vehicleInfo, damageDetails, userDescription);

        //시스템 메시지 생성
        String systemMessage = promptBuilder.buildSystemMessage();

        //사용자 프롬프트 생성
        String userPrompt = promptBuilder.buildUserPrompt(llmRequest);

        //LLM Client 호출
        return llmClient.call(systemMessage, userPrompt);
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
