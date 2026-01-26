package refresh.acci.domain.repair.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class RepairEstimateCreatedEvent {
    private final UUID estimateId;
}
