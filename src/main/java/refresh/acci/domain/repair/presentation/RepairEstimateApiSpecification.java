package refresh.acci.domain.repair.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateRequest;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateResponse;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateSummaryResponse;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.global.common.PageResponse;
import refresh.acci.global.exception.ErrorResponseEntity;

import java.util.UUID;

@Tag(name = "Repair Estimate (수리비 견적)", description = "수리비 견적 관련 API")
public interface RepairEstimateApiSpecification {

    @Operation(
            summary = "수리비 견적 요청",
            description = "차량 정보와 손상 내역을 바탕으로 AI가 수리비 견적을 산출합니다. <br><br>" +
                    "LLM이 각 부위별 수리 방법과 비용을 분석하여 총 견적을 제공합니다. <br><br>" +
                    "이미지는 선택 사항이며, 첨부 시 LLM이 이미지를 추가 참고자료로 활용합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)
                    )
            ),
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
            @RequestPart("request") RepairEstimateRequest request,
            @RequestPart(value = "image", required = false)
            @Parameter(description = "차량 손상 이미지 (선택, jpg/png)", schema = @Schema(type = "string", format = "binary"))
            MultipartFile image,
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

    @Operation(
            summary = "수리비 견적 페이징 조회",
            description = "인증된 사용자의 수리비 견적 기록을 페이징하여 조회합니다. <br><br>" +
                    "최신순으로 정렬되며, 기본 페이지 크기는 5개입니다. <br><br>" +
                    "요약 정보(견적 ID, 상태, 총 견적 금액, 차량 모델, 손상 요약, 생성일)만 포함되며, " +
                    "상세 정보는 단건 조회 API를 사용하세요. <br><br>" +
                    "이 API는 인증이 필요하며, HttpOnly 쿠키에 저장된 Access Token이 자동으로 전송됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수리비 견적 기록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PageResponse.class))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증되지 않은 사용자",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                "code": 401,
                                                "name": "JWT_ENTRY_POINT",
                                                "message": "인증되지 않은 사용자입니다.",
                                                "errors": null
                                            }
                                            """)))
            })
    ResponseEntity<PageResponse<RepairEstimateSummaryResponse>> getEstimates(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "5")
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );
}