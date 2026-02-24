package refresh.acci.domain.repair.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import refresh.acci.domain.repair.application.facade.RepairEstimateFacade;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateRequest;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateResponse;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateSummaryResponse;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.global.common.PageResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/repair-estimates")
public class RepairEstimateController implements RepairEstimateApiSpecification{

    private final RepairEstimateFacade repairEstimateFacade;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RepairEstimateResponse> createEstimate(
            @RequestPart("request") RepairEstimateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RepairEstimateResponse response = repairEstimateFacade.createEstimate(request, images, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{repairEstimateId}")
    public ResponseEntity<RepairEstimateResponse> getEstimate(@PathVariable UUID repairEstimateId) {
        RepairEstimateResponse response = repairEstimateFacade.getEstimate(repairEstimateId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<RepairEstimateSummaryResponse>> getEstimates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PageResponse<RepairEstimateSummaryResponse> response = repairEstimateFacade.getUserEstimates(userDetails.getId(), page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
