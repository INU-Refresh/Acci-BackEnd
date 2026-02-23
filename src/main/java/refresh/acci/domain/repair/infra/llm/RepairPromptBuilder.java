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

    //시스템 메시지 생성
    public String buildSystemMessage() {
        return """
        당신은 자동차 수리비 견적 전문가입니다.
        차량 정보와 손상 내역을 분석하여 정확한 수리비를 산출해야 합니다.
        
        응답은 반드시 JSON 형식으로 작성하세요.
        한국 시장 기준 수리비를 반영하세요.
        각 부위별로 수리 방법(replace/repair/paint/repair_and_paint)과 비용을 명시하세요.
        
        응답 JSON 형식:
        {
          "repair_items": [
            {
              "part_name": "부위명",
              "repair_method": "replace|repair|paint|repair_and_paint",
              "cost": 수리비(숫자)
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
    public RepairEstimateLlmRequest toLlmRequest(VehicleInfo vehicleInfo, List<DamageDetail> damageDetails, String userDescription) {
        return RepairEstimateLlmRequest.builder()
                .vehicleInfo(buildVehicleInfoDto(vehicleInfo))
                .damageDetails(buildDamageDetailDtos(damageDetails))
                .userDescription(userDescription)
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
                        .build())
                .toList();
    }
}