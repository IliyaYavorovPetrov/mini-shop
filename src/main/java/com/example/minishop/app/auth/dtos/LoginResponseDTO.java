package com.example.minishop.app.auth.dtos;

public record LoginResponseDTO(String token, String userID, String userRole, String userAuthProvider) {
}
