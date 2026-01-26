package refresh.acci.domain.repair.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import refresh.acci.domain.repair.application.RepairEstimateService;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateRequest;
import refresh.acci.domain.repair.presentation.dto.RepairEstimateResponse;
import refresh.acci.domain.user.model.CustomUserDetails;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/repair-estimates")
public class RepairEstimateController implements RepairEstimateApiSpecification{

    private final RepairEstimateService repairEstimateService;

    @PostMapping
    public ResponseEntity<RepairEstimateResponse> createEstimate(@RequestBody RepairEstimateRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        RepairEstimateResponse response = repairEstimateService.createEstimate(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{repairEstimateId}")
    public ResponseEntity<RepairEstimateResponse> getEstimate(@PathVariable UUID repairEstimateId) {
        RepairEstimateResponse response = repairEstimateService.getEstimate(repairEstimateId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
