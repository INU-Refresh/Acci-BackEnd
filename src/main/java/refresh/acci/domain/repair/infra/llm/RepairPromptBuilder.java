package refresh.acci.domain.repair.infra.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import refresh.acci.domain.repair.infra.llm.dto.RepairEstimateLlmRequest;
import refresh.acci.domain.repair.model.DamageDetail;
import refresh.acci.domain.repair.model.VehicleInfo;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RepairPromptBuilder {

    private final ObjectMapper objectMapper;

    /**
     * LLM 시스템 메시지를 생성
     * 역할 정의 + 강제 규칙(severity→method) + 가격 가이드라인 + 응답 포맷을 결합
     */
    public String buildSystemMessage() {
        return buildRoleDefinition()
                + RepairCostReference.buildSeverityRuleText()
                + RepairCostReference.buildPriceGuidelineText()
                + buildResponseFormatText();
    }

    private String buildRoleDefinition() {
        return """
                당신은 현대자동차·기아·제네시스 차량 전문 수리비 견적 전문가입니다.
                한국 시장의 공업사(1급 정비소) 기준으로 수리비를 산출합니다.
                
                [기본 원칙]
                - 수리비 = 부품비 + 공임비 + 도색비 등 수리에 필요한 모든 비용의 합산
                - 한국 시장 기준의 현실적인 가격을 산출하세요
                - 아래 제공된 "손상 심각도별 수리 방법 결정 규칙"과 "가격 가이드라인"을 반드시 참고하세요
                - 이미지가 제공된 경우, 이미지의 손상 상태도 함께 참고하여 판단하세요
                
                """;
    }

    private String buildResponseFormatText() {
        return """
                === 응답 규칙 ===
                1. 응답은 반드시 아래 JSON 형식으로만 작성하세요. JSON 외 다른 텍스트를 포함하지 마세요.
                2. 입력된 각 damage_detail에 대해 정확히 1개의 repair_item을 생성하세요.
                3. part_name은 입력된 한국어 부위명(part_name_kr)을 그대로 사용하세요.
                4. repair_method는 반드시 다음 중 하나: "replace", "repair", "paint", "repair_and_paint"
                5. cost는 만원 단위가 아닌 원(₩) 단위 정수로 작성하세요. (예: 45만원 → 450000)
                6. 차량의 segment(차급)에 해당하는 배율을 적용한 금액을 산출하세요.
                
                응답 JSON 형식:
                {
                  "repair_items": [
                    {
                      "part_name": "부위명 (한국어)",
                      "repair_method": "replace|repair|paint|repair_and_paint",
                      "cost": 수리비(원 단위 정수)
                    }
                  ]
                }
                """;
    }

    //사용자 프롬프트 생성, RepairEstimateLlmRequest 객체를 JSON으로 변환
    public String buildUserPrompt(RepairEstimateLlmRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.error("[RepairEstimate] 프롬프트 생성 실패", e);
            throw new CustomException(ErrorCode.LLM_API_CALL_FAILED);
        }
    }

    //엔티티를 LLM요청 DTO로 변환
    public RepairEstimateLlmRequest toLlmRequest(VehicleInfo vehicleInfo, List<DamageDetail> damageDetails) {
        return RepairEstimateLlmRequest.builder()
                .vehicleInfo(buildVehicleInfoDto(vehicleInfo))
                .damageDetails(buildDamageDetailDtos(damageDetails))
                .build();
    }

    //VehicleInfo 엔티티 -> DTO 변환
    private RepairEstimateLlmRequest.VehicleInfo buildVehicleInfoDto(VehicleInfo vehicleInfo) {
        return RepairEstimateLlmRequest.VehicleInfo.builder()
                .brand(vehicleInfo.getBrand().getCode())
                .model(vehicleInfo.getModel())
                .year(vehicleInfo.getYear())
                .vehicleType(vehicleInfo.getVehicleType().getCode())
                .segment(vehicleInfo.getSegment().getCode())
                .build();
    }

    //DamageDetail 엔티티 리스트 -> DTO 리스트 변환
    private List<RepairEstimateLlmRequest.DamageDetail> buildDamageDetailDtos(List<DamageDetail> damageDetails) {
        return damageDetails.stream()
                .map(damage -> RepairEstimateLlmRequest.DamageDetail.builder()
                        .partNameEn(damage.getPartNameEn())
                        .partNameKr(damage.getPartNameKr())
                        .damageSeverity(damage.getDamageSeverity().getCode())
                        .userDescription(damage.getUserDescription())
                        .build())
                .toList();
    }
}