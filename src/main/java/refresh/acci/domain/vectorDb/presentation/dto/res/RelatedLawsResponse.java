package refresh.acci.domain.vectorDb.presentation.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

public record RelatedLawsResponse(

        @Schema(description = "법률 이름", example = "도로교통법 제17조(자동차등과 노면전차의 속도)")
        String lawName,

        @Schema(description = "법률 내용", example = "자동차등의 운전자는 최고속도보다 빠르게 운전하거나 부득이한 사유 없이 최저속도보다 느리게 운전하여서는 아니 된다.")
        String lawContent
) {
}
