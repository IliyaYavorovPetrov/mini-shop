package com.example.minishop.app.auth;

import com.example.minishop.app.auth.dtos.SignInResponseDTO;
import com.example.minishop.app.auth.dtos.SignUpResponseDTO;
import com.example.minishop.app.auth.models.SignInModel;
import com.example.minishop.app.auth.models.SignUpModel;
import com.example.minishop.app.users.UserRoleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class AuthMapper {
    private static final Logger logger = LoggerFactory.getLogger(AuthMapper.class);

    public static SignUpResponseDTO fromSignUpModelToSignUpRequestDTO(SignUpModel signUpModel) {
        return new SignUpResponseDTO(
                signUpModel.getUserID(),
                signUpModel.getUserRole(),
                signUpModel.getAuthProvider()
        );
    }

    public static SignInResponseDTO fromSignInModelToSignInRequestDTO(SignInModel signInModel) {
        return new SignInResponseDTO(
                signInModel.getUserID(),
                signInModel.getUserRole(),
                signInModel.getAuthProvider()
        );
    }

    public static AuthProviderType getAuthProviderTypeFromStringOrDefault(String authProviderType) {
        AuthProviderType userAuthProviderType = AuthProviderType.GOOGLE;

        if (EnumSet.allOf(AuthProviderType.class).stream().noneMatch(ap -> ap.name().equals(authProviderType))) {
            logger.error("Invalid auth provider: {}, by default is used {}", authProviderType, userAuthProviderType.name());
            return userAuthProviderType;
        }

        userAuthProviderType = AuthProviderType.valueOf(authProviderType);
        return userAuthProviderType;
    }
}
