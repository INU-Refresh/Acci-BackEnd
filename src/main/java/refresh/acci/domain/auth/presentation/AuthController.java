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
public class AuthController implements AuthApiSpecification{

    private final AuthService authService;

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> exchangeToken(@Valid @RequestBody TokenExchangeRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = authService.exchangeCodeForToken(request.getCode(), response);
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh (@CookieValue(name = "refreshToken") String refreshToken, HttpServletResponse response) {
        TokenResponse tokenResponse = authService.refresh(refreshToken, response);
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout (@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response) {
        authService.logout(userDetails.getName(), response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
