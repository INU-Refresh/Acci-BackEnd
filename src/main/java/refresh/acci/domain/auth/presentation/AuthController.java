package refresh.acci.domain.auth.presentation;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import refresh.acci.domain.auth.application.AuthService;
import refresh.acci.domain.auth.presentation.dto.TokenExchangeRequest;
import refresh.acci.domain.auth.presentation.dto.TokenResponse;
import refresh.acci.domain.user.model.CustomUserDetails;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> exchangeToken(@Valid @RequestBody TokenExchangeRequest request) {
        TokenResponse response = authService.exchangeCodeForToken(request.getCode());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh (@CookieValue(name = "refreshToken") String refreshToken) {
        TokenResponse response = authService.refresh(refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout (@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response) {
        authService.logout(userDetails.getName(), response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
