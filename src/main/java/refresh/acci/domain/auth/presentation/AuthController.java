package refresh.acci.domain.auth.presentation;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refresh.acci.domain.auth.application.AuthService;
import refresh.acci.domain.auth.presentation.dto.TokenResponse;
import refresh.acci.domain.user.model.CustomUserDetails;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

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
