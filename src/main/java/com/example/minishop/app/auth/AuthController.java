package com.example.minishop.app.auth;

import com.example.minishop.app.auth.dtos.SignInRequestDTO;
import com.example.minishop.app.auth.dtos.SignInResponseDTO;
import com.example.minishop.app.auth.dtos.SignUpRequestDTO;
import com.example.minishop.app.auth.dtos.SignUpResponseDTO;
import com.example.minishop.app.auth.models.SignInModel;
import com.example.minishop.app.auth.models.SignUpModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("${app.base-path}/user")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponseDTO> signUp(
            @RequestBody SignUpRequestDTO signUpRequestDTO
    ) throws IllegalAccessException {
        Optional<SignUpModel> signUpResult = authService.signUp(
                signUpRequestDTO,
                AuthProviderType.valueOf(signUpRequestDTO.authProvider())
        );
        if (signUpResult.isEmpty()) {
            throw new IllegalAccessException();
        }

        ResponseCookie cookieAuthToken = authService.getCookie(
                AuthService.COOKIE_AUTH_TOKEN_NAME,
                signUpResult.get().getToken(),
                AuthService.TOKEN_VALIDITY_SECS
        );

        ResponseCookie cookieAuthProvider = authService.getCookie(
                AuthService.COOKIE_AUTH_PROVIDER_NAME,
                signUpResult.get().getAuthProvider(),
                AuthService.TOKEN_VALIDITY_SECS
        );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookieAuthToken.toString())
                .header(HttpHeaders.SET_COOKIE, cookieAuthProvider.toString())
                .body(AuthMapper.fromSignUpModelToSignUpRequestDTO(signUpResult.get()));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponseDTO> signIn(
            @RequestBody SignInRequestDTO signInRequestDTO
    ) throws IllegalAccessException {
        Optional<SignInModel> signInResult = authService.signIn(
                signInRequestDTO,
                AuthProviderType.valueOf(signInRequestDTO.authProvider())
        );
        if (signInResult.isEmpty()) {
            throw new IllegalAccessException();
        }

        ResponseCookie cookieAuthToken = authService.getCookie(
                AuthService.COOKIE_AUTH_TOKEN_NAME,
                signInResult.get().getToken(),
                AuthService.TOKEN_VALIDITY_SECS
        );

        ResponseCookie cookieAuthProvider = authService.getCookie(
                AuthService.COOKIE_AUTH_PROVIDER_NAME,
                signInResult.get().getAuthProvider(),
                AuthService.TOKEN_VALIDITY_SECS
        );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookieAuthToken.toString())
                .header(HttpHeaders.SET_COOKIE, cookieAuthProvider.toString())
                .body(AuthMapper.fromSignInModelToSignInRequestDTO(signInResult.get()));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut() {
        ResponseCookie cookieAuthToken = authService.getCookie(
                AuthService.COOKIE_AUTH_TOKEN_NAME,
                "",
                AuthService.TOKEN_VALIDITY_SECS
        );

        ResponseCookie cookieAuthProvider = authService.getCookie(
                AuthService.COOKIE_AUTH_PROVIDER_NAME,
                "",
                AuthService.TOKEN_VALIDITY_SECS
        );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookieAuthToken.toString())
                .header(HttpHeaders.SET_COOKIE, cookieAuthProvider.toString())
                .build();
    }
}
