package com.example.minishop.app.auth;

import com.example.minishop.app.auth.dtos.LoginRequestDTO;
import com.example.minishop.base.BaseController;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController extends BaseController {
    @PostMapping("/login")
    public ResponseEntity<Void> LoginWithGoogleOauth2(@RequestBody LoginRequestDTO requestBody, HttpServletResponse response) {
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
}
