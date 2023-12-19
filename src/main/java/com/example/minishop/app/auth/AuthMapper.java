package com.example.minishop.app.auth;

import com.example.minishop.app.auth.dtos.SignUpResponseDTO;
import com.example.minishop.app.auth.models.SignUpModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthMapper {
    private static final Logger logger = LoggerFactory.getLogger(AuthMapper.class);

    public static SignUpResponseDTO fromSignUpModelToSignUpRequestDTO(SignUpModel signUpModel) {
        return new SignUpResponseDTO(
                signUpModel.getUserID(),
                signUpModel.getUserRole(),
                signUpModel.getAuthProvider()
        );
    }
}
