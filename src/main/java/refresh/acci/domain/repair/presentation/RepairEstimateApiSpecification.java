package refresh.acci.domain.repair.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateRequest;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateResponse;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.global.exception.ErrorResponseEntity;

import java.util.UUID;

@Tag(name = "Repair Estimate (수리비 견적)", description = "수리비 견적 관련 API")
public interface RepairEstimateApiSpecification {

    @Operation(
            summary = "수리비 견적 요청",
            description = "차량 정보와 손상 내역을 바탕으로 AI가 수리비 견적을 산출합니다. <br><br>" +
                    "LLM이 각 부위별 수리 방법과 비용을 분석하여 총 견적을 제공합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "수리비 견적 생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RepairEstimateResponse.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 데이터",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "code": 400,
                                                "name": "INVALID_VEHICLE_BRAND",
                                                "message": "유효하지 않은 차량 브랜드입니다.",
                                                "errors": null
                                            }
                                            """))),
                    @ApiResponse(
                            responseCode = "500",
                            description = "LLM API 호출 실패",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "code": 500,
                                                "name": "LLM_API_CALL_FAILED",
                                                "message": "LLM API 호출에 실패했습니다.",
                                                "errors": null
                                            }
                                            """)))
            })
    ResponseEntity<RepairEstimateResponse> createEstimate(
            @RequestBody RepairEstimateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(
            summary = "수리비 견적 조회",
            description = "생성된 수리비 견적의 상세 정보를 조회합니다. <br><br>" +
                    "차량 정보, 손상 내역, 부위별 수리 항목, 총 견적 금액을 제공합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수리비 견적 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RepairEstimateResponse.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "견적을 찾을 수 없음",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "code": 404,
                                                "name": "REPAIR_ESTIMATE_NOT_FOUND",
                                                "message": "수리비 견적을 찾을 수 없습니다.",
                                                "errors": null
                                            }
                                            """)))
            })
    ResponseEntity<RepairEstimateResponse> getEstimate(@PathVariable UUID estimateId);
}
