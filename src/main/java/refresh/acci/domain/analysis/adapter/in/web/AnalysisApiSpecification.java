package refresh.acci.domain.analysis.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import refresh.acci.domain.analysis.adapter.in.web.dto.res.AnalysisResultResponse;
import refresh.acci.domain.analysis.adapter.in.web.dto.res.AnalysisSummaryResponse;
import refresh.acci.domain.analysis.adapter.in.web.dto.res.AnalysisUploadResponse;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.global.common.PageResponse;
import refresh.acci.global.exception.ErrorResponseEntity;

import java.util.UUID;

@Tag(name = "Analysis (분석)", description = "Analysis (분석) 관련 API")
public interface AnalysisApiSpecification {

    @Operation(
            summary = "영상 분석 업로드",
            description = "교통사고 영상을 업로드하여 AI 서버에 전송합니다. 업로드 후 분석 ID를 반환합니다. <br><br>" +
                    "분석 ID를 사용하여 분석 결과를 조회하거나 SSE 구독을 할 수 있습니다. <br><br>" +
                    "파일 확장자가 .mp4, .avi 중 하나가 아닐 경우 에러를 발생시킵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "영상 분석 업로드 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AnalysisUploadResponse.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "지원하지 않는 파일 형식입니다.",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(value = """
                                      {
                                         "code": 400,
                                         "name": "UNSUPPORTED_FILE_TYPE",
                                         "message": "지원하지 않는 파일 형식입니다.",
                                         "errors": null
                                      }
                                      """))),
                    @ApiResponse(
                            responseCode = "500",
                            description = "분석이 중단되었습니다.",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(value = """
                                      {
                                         "code": 500,
                                         "name": "ANALYSIS_INTERRUPTED",
                                         "message": "분석이 중단되었습니다.",
                                         "errors": null
                                      }
                                      """)))
            })
    ResponseEntity<AnalysisUploadResponse> analyze(
            @RequestPart("video") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "[SSE 구독] 영상 분석 상태 조회",
            description = "영상 분석 진행 상태를 SSE(Server-Sent Events)로 구독합니다. <br><br>" +
                    "클라이언트는 이 엔드포인트에 연결하여 실시간으로 분석 상태 업데이트를 받을 수 있습니다. <br><br>",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "SSE 구독 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = SseEmitter.class)))
            })
    SseEmitter subscribe(
            @PathVariable UUID analysisId
    );

    @Operation(
            summary = "랜덤 Tip 제공",
            description = "영상 분석 로딩 화면에서의 랜덤한 Tip 제공을 위한 API 입니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "랜덤 Tip 제공 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class)))
            })
    ResponseEntity<String> getLoadingTips();

    @Operation(
            summary = "영상 분석 결과 조회",
            description = "영상 분석이 완료된 후 해당 UUID를 기반으로 분석 결과를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "영상 분석 결과 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AnalysisUploadResponse.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "분석을 찾을 수 없습니다.",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(value = """
                                      {
                                         "code": 404,
                                         "name": "ANALYSIS_NOT_FOUND",
                                         "message": "분석을 찾을 수 없습니다.",
                                         "errors": null
                                      }
                                      """)))
            })
    ResponseEntity<AnalysisResultResponse> getAnalysisResult(
            @PathVariable UUID analysisId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "분석 기록 페이징 조회",
            description = "인증된 사용자의 영상 분석 기록을 페이징하여 조회합니다. <br><br>" +
                    "최신순으로 정렬되며, 기본 페이지 크기는 5개입니다. <br><br>" +
                    "요약 정보(분석 ID, 상태, 과실 비율, 생성일)만 포함되며, 상세 정보는 단건 조회 API를 사용하세요. <br><br>" +
                    "이 API는 인증이 필요하며, HttpOnly 쿠키에 저장된 Access Token이 자동으로 전송됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "분석 기록 조회 성공",
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
    ResponseEntity<PageResponse<AnalysisSummaryResponse>> getAnalyses(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "5")
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "분석 영상 URL 조회",
            description = "해당 분석의 영상 URL을 조회합니다. <br><br>" +
                    "해당 분석이 인증된 회원의 소유가 아닐 경우 접근이 거부됩니다. <br><br>" +
                    "영상 URL은 10분이 지나면 만료됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "분석 영상 URL 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(
                            responseCode = "403",
                            description = "해당 분석에 접근할 권한이 없습니다.",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(value = """
                                      {
                                         "code": 403,
                                         "name": "ACCESS_DENIED_TO_ANALYSIS",
                                         "message": "해당 분석에 접근할 권한이 없습니다.",
                                         "errors": null
                                      }
                                      """)))
            })
    ResponseEntity<String> getVideoUrl(
            @PathVariable UUID analysisId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );
}