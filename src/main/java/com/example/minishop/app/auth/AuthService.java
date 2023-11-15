package com.example.minishop.app.auth;

import com.example.minishop.app.auth.dtos.IdTokenRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public String loginOAuthGoogle(IdTokenRequestDTO requestBody) {
        return "jwt-token";
    }
}
