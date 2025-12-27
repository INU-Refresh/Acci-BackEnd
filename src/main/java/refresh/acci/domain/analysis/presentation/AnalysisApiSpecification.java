package refresh.acci.domain.analysis.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import refresh.acci.domain.analysis.presentation.dto.res.AnalysisResultResponse;
import refresh.acci.domain.analysis.presentation.dto.res.AnalysisUploadResponse;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.global.exception.ErrorResponseEntity;

import java.util.UUID;

@Tag(name = "Analysis (분석)", description = "Analysis (분석) 관련 API")
public interface AnalysisApiSpecification {

    @Operation(
            summary = "영상 분석 업로드",
            description = "교통사고 영상을 업로드하여 AI 서버에 전송합니다. 업로드 후 분석 ID를 반환합니다. <br><br>" +
                    "분석 ID를 사용하여 분석 결과를 조회하거나 SSE 구독을 할 수 있습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "영상 분석 업로드 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AnalysisUploadResponse.class)))
    })
    ResponseEntity<AnalysisUploadResponse> analyze(@RequestPart("video") MultipartFile file,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(
            summary = "[SSE 구독] 영상 분석 상태 조회",
            description = "영상 분석 진행 상태를 SSE(Server-Sent Events)로 구독합니다. <br><br>" +
                    "클라이언트는 이 엔드포인트에 연결하여 실시간으로 분석 상태 업데이트를 받을 수 있습니다. <br><br>" +
                    "파일 확장자가 .mp4, .avi 중 하나가 아닐 경우 에러를 발생시킵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "SSE 구독 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = SseEmitter.class))),
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
                                      """)))
    })
    SseEmitter subscribe(@PathVariable UUID analysisId);

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
    ResponseEntity<AnalysisResultResponse> getAnalysisResult(@PathVariable UUID analysisId);
}
