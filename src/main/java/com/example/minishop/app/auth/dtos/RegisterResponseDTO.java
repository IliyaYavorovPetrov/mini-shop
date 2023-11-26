package com.example.minishop.app.auth.dtos;

public record RegisterResponseDTO(String token, String userID, String userRole, String userAuthProvider) {
}
