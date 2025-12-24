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
import refresh.acci.domain.user.application.MyPageService;
import refresh.acci.domain.user.model.CustomUserDetails;
import refresh.acci.domain.user.presentation.dto.MyPageResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
public class MyPageController {
    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        MyPageResponse response = myPageService.getMyPage(userDetails.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /*
    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response) {
        myPageService.deleteAccount(userDetails.getId(), response);
        return ResponseEntity.noContent().build();
    }
     */
}
