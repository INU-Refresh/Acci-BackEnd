package refresh.acci.domain.analysis.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import refresh.acci.domain.analysis.application.AnalysisService;
import refresh.acci.domain.analysis.application.AnalysisSseService;
import refresh.acci.domain.analysis.presentation.dto.res.AnalysisResultResponse;
import refresh.acci.domain.analysis.presentation.dto.res.AnalysisSummaryResponse;
import refresh.acci.domain.analysis.presentation.dto.res.AnalysisUploadResponse;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.global.common.PageResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analyses")
@RequiredArgsConstructor
public class AnalysisController implements AnalysisApiSpecification{

    private final AnalysisService analysisService;
    private final AnalysisSseService analysisSseService;

    // 영상 분석 업로드
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisUploadResponse> analyze(
            @RequestPart("video") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(analysisService.anaylze(file, userDetails));
    }

    // [SSE 구독] 영상 분석 상태 조회
    @GetMapping(value = "/{analysisId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @PathVariable UUID analysisId
    ) {
        return analysisSseService.subscribe(analysisId);
    }

    // 랜덤 Tip 제공
    @GetMapping("/loading")
    public ResponseEntity<String> getLoadingTips() {
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getLoadingTips());
    }

    // 영상 분석 결과 조회
    @GetMapping("/{analysisId}")
    public ResponseEntity<AnalysisResultResponse> getAnalysisResult(
            @PathVariable UUID analysisId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getAnalysisResult(analysisId));
    }

    // 영상 분석 결과 페이징 조회
    @GetMapping
    public ResponseEntity<PageResponse<AnalysisSummaryResponse>> getAnalyses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PageResponse<AnalysisSummaryResponse> response = analysisService.getUserAnalyses(
                userDetails.getId(), page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    // 분석 영상 URL 조회
    @GetMapping("/{analysisId}/video")
    public ResponseEntity<String> getVideoUrl(
            @PathVariable UUID analysisId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getVideoUrl(analysisId, userDetails));
    }
}
