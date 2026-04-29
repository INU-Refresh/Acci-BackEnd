package refresh.acci.domain.analysis.adapter.out.ai.dto.res;

public record AiResultResponse(

        String job_id,

        int accident_type,

        int vehicle_A_fault,

        int vehicle_B_fault,

        ClassificationInfoResponse classification_info

) {
}
