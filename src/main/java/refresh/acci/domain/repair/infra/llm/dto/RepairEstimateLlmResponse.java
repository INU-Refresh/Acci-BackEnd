package refresh.acci.domain.repair.infra.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RepairEstimateLlmResponse {

    @JsonProperty("repair_items")
    private List<RepairItem> repairItems;


    @Getter
    @NoArgsConstructor
    public static class RepairItem {
        @JsonProperty("part_name")
        private String partName;

        @JsonProperty("repair_method")
        private String repairMethod;

        private Long cost;
    }
}
