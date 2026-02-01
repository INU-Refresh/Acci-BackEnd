package refresh.acci.domain.analysis.presentation.dto.res;

public record AiResultResponse(

        int accident_type,

        int vehicle_A_fault,

        int vehicle_B_fault,

        String place,

        String situation,

        String vehicle_a,

        String vehicle_b

) {
}
