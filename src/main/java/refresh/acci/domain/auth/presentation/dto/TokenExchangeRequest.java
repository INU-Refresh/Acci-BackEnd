package refresh.acci.domain.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenExchangeRequest {
    @NotBlank(message = "인증 코드는 필수입니다.")
    private String code;
}
