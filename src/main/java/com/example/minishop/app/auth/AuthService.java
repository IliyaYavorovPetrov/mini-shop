package com.example.minishop.app.auth;

import com.example.minishop.app.auth.dtos.LoginRequestDTO;
import com.example.minishop.app.auth.dtos.LoginResponseDTO;
import com.example.minishop.app.auth.dtos.RegisterRequestDTO;
import com.example.minishop.app.auth.dtos.RegisterResponseDTO;
import com.example.minishop.app.users.UserRoleType;
import com.example.minishop.app.users.UsersService;
import com.example.minishop.app.users.models.UserModel;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

import static com.example.minishop.app.users.UserMapper.fromUserModelToUserRequestDTO;

@Service
public class AuthService {
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UsersService usersService;
    private final JWTUtils jwtUtils;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public AuthService(@Value("${app.oauth2.provider.google.id}") String googleClientID, UsersService usersService, JWTUtils jwtUtils) {
        this.usersService = usersService;
        this.jwtUtils = jwtUtils;
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleClientID))
                .build();
    }

    public RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO, AuthProviderType authProviderType) {
        Optional<UserModel> userFromProvider = verifyToken(registerRequestDTO.token(), authProviderType);
        if (userFromProvider.isEmpty()) {
            throw new IllegalArgumentException();
        }

        String userID = usersService.createUser(fromUserModelToUserRequestDTO(userFromProvider.get()));

        return new RegisterResponseDTO(
                jwtUtils.createToken(userFromProvider.get(), authProviderType),
                userID,
                UserRoleType.CLIENT.name(),
                authProviderType.name()
        );
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO, AuthProviderType authProviderType) {
        Optional<UserModel> userFromProvider = verifyToken(loginRequestDTO.token(), authProviderType);
        if (userFromProvider.isEmpty()) {
            throw new IllegalArgumentException();
        }

        Optional<UserModel> user = usersService.getUserById(userFromProvider.get().getId());
        if (user.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return new LoginResponseDTO(
                jwtUtils.createToken(user.get(), authProviderType),
                user.get().getId(),
                user.get().getRole().toString(),
                authProviderType.name()
        );
    }

    private Optional<UserModel> verifyToken(String token, AuthProviderType authProviderType) {
        try {
            if (authProviderType.equals(AuthProviderType.GOOGLE)) {
                GoogleIdToken googleIDToken = googleIdTokenVerifier.verify(token);
                if (googleIDToken == null) {
                    return Optional.empty();
                }

                GoogleIdToken.Payload payload = googleIDToken.getPayload();
                String id = payload.getSubject();
                String name = (String) payload.get("name");
                String email = payload.getEmail();
                String imageURL = (String) payload.get("image");

                return Optional.of(new UserModel(id, name, email, imageURL, UserRoleType.CLIENT));
            }
        } catch (Exception ex) {
            logger.error("Failed to verify");
        }

        return Optional.empty();
    }
}
