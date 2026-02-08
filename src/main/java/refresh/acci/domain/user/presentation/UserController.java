package refresh.acci.domain.user.presentation;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refresh.acci.domain.user.application.facade.UserFacade;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.domain.user.presentation.dto.UserInfoResponse;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
public class UserController implements UserApiSpecification {
    private final UserFacade userFacade;

    @GetMapping
    public ResponseEntity<UserInfoResponse> getMyPage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoResponse response = userFacade.getUserInfo(userDetails.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response) {
        userFacade.deleteUserAccount(userDetails.getId(), response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

}
