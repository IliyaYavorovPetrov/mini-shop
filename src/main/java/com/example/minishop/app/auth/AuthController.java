package com.example.minishop.app.auth;

import com.example.minishop.app.auth.dtos.SignInRequestDTO;
import com.example.minishop.app.auth.dtos.SignInResponseDTO;
import com.example.minishop.app.auth.dtos.SignUpRequestDTO;
import com.example.minishop.app.auth.dtos.SignUpResponseDTO;
import com.example.minishop.app.auth.models.SignUpModel;
import com.example.minishop.app.users.models.UserModel;
import com.example.minishop.base.BaseController;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class AuthController extends BaseController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponseDTO> signUp(
            @RequestBody SignUpRequestDTO signUpRequestDTO,
            HttpServletResponse response
    ) throws IllegalAccessException {
        Optional<SignUpModel> signUpResult = authService.signUp(signUpRequestDTO, AuthProviderType.valueOf(signUpRequestDTO.authProvider()));
        if (signUpResult.isEmpty()) {
            throw new IllegalAccessException();
        }

        String authToken = signUpResult.get().getToken();
        final ResponseCookie cookie = ResponseCookie.from("AUTH-TOKEN", authToken)
                .httpOnly(true)
                .maxAge(7 * 24 * 3600)
                .path("/")
                .secure(false)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(AuthMapper.fromSignUpModelToSignUpRequestDTO(signUpResult.get()));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponseDTO> signIn(
            @RequestBody SignInRequestDTO signInRequestDTO,
            HttpServletResponse response
    ) {
        String authToken = "some_token";
        final ResponseCookie cookie = ResponseCookie.from("AUTH-TOKEN", authToken)
                .httpOnly(true)
                .maxAge(7 * 24 * 3600)
                .path("/")
                .secure(false)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(
            HttpServletResponse response
    ) {
        response.addHeader(HttpHeaders.SET_COOKIE, "");
        return ResponseEntity.ok().build();
    }
}
